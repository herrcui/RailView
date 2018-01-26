import tensorflow as tf
import math
import numpy as np
import random
import sys
import logging
import gc
from py4j.java_collections import SetConverter, MapConverter, ListConverter

# https://github.com/awjuliani/DeepRL-Agents/blob/master/Double-Dueling-DQN.ipynb

class QNetwork():
    NUM_FILTER1 = 64
    NUM_FILTER2 = 32

    KERNEL_SIZE1 = [3, 3]
    KERNEL_SIZE2 = [3, 3]

    POOL_STRIDE_SIZE1 = [2, 2]
    POOL_STRIDE_SIZE2 = [2, 2]

    NUM_DENSE = 16
    NUM_HIDDEN = 4
    
    keep_prob = 0.25

    def __init__(self, num_resource, num_timeslot, num_train):
        # input for calculation
        self.state_ph = tf.placeholder(tf.float32, shape = [None, num_resource, num_timeslot, num_train])
        
        sum_state_ph = tf.reduce_sum(self.state_ph, 3, keep_dims=True)
        
        input_layer = tf.reshape(sum_state_ph, [-1, num_resource, num_timeslot, 1]) # batch, width, height, channel
        
        conv1 = tf.layers.conv2d(
              inputs=input_layer,
              filters=self.NUM_FILTER1,
              kernel_size=self.KERNEL_SIZE1,
              padding="same",
              activation=tf.nn.relu) 
        # conv1 = batch, num_resource, num_timeslot, NUM_FILTER1
        
        pool1 = tf.layers.max_pooling2d(inputs=conv1, pool_size=self.POOL_STRIDE_SIZE1, strides=self.POOL_STRIDE_SIZE1)
        # pool1 = batch, num_resource/POOL_STRIDE_SIZE1, num_timeslot/POOL_STRIDE_SIZE1, NUM_FILTER1
        
        conv2 = tf.layers.conv2d(
              inputs=pool1,
              filters=self.NUM_FILTER2,
              kernel_size=self.KERNEL_SIZE2,
              padding="same",
              activation=tf.nn.relu)
        # conv2 = batch, num_resource/POOL_STRIDE_SIZE1, num_timeslot/POOL_STRIDE_SIZE1, NUM_FILTER2
        
        pool2 = tf.layers.max_pooling2d(inputs=conv2, pool_size=self.POOL_STRIDE_SIZE2, strides=self.POOL_STRIDE_SIZE2)
        # pool2 = batch, num_resource/POOL_STRIDE_SIZE1/POOL_STRIDE_SIZE2, num_timeslot/POOL_STRIDE_SIZE1/POOL_STRIDE_SIZE2, NUM_FILTER2
        
        num_width = math.floor(math.floor(num_resource/self.POOL_STRIDE_SIZE1[0]) / self.POOL_STRIDE_SIZE2[0])
        num_height = math.floor(math.floor((num_timeslot)/self.POOL_STRIDE_SIZE1[1]) / self.POOL_STRIDE_SIZE2[1])
        
        conv2_flat = tf.reshape(pool2, [-1, int(num_width * num_height * self.NUM_FILTER2)])
        
        dense = tf.layers.dense(inputs=conv2_flat, units=self.NUM_DENSE, activation=tf.nn.sigmoid)
        
        dropout = tf.layers.dropout(inputs=dense, rate=self.keep_prob)
        
        hidden = tf.layers.dense(inputs=dropout, units=self.NUM_HIDDEN, activation=tf.nn.sigmoid)
        
        logits = tf.reshape(tf.layers.dense(inputs=hidden, units=1), [-1])
        
        self.predict = tf.nn.sigmoid(logits)
        
        # for training
        self.targetQ = tf.placeholder(shape=[None],dtype=tf.float32)
        self.loss = tf.reduce_mean(tf.square(self.targetQ - self.predict))
        trainer = tf.train.AdamOptimizer(learning_rate=0.0001)
        self.update = trainer.minimize(self.loss)
        
        tf.summary.scalar('Loss', self.loss)
                
        self.summary_op = tf.summary.merge_all()

class Experience():
    # the state_action will be saved in the form as a matrix, 
    # it is build by build_state_actions() in RLDispatchingServer
    def __init__(self, state_action, reward, next_state_actions):
        self.state_action = state_action
        self.reward = reward
        self.next_state_actions = next_state_actions
        
class ExperienceBuffer():
    def __init__(self, buffer_size = 5000):
        self.buffer = []
        self.buffer_size = buffer_size
    
    def extend_experiences(self, experiences):
        if len(self.buffer) + len(experiences) >= self.buffer_size:
            self.buffer[0:(len(experiences)+len(self.buffer))-self.buffer_size] = []
        self.buffer.extend(experiences)
            
    def sample(self, size):
        return np.array(random.sample(self.buffer, size))   

# These functions allow us to update the parameters of our target network with those of the primary network.
def updateTargetGraph(tfVars, tau):
    total_vars = len(tfVars)
    op_holder = []
    for idx,var in enumerate(tfVars[0:total_vars//2]):
        op_holder.append(tfVars[idx+total_vars//2].assign((var.value()*tau) + ((1-tau)*tfVars[idx+total_vars//2].value())))
    return op_holder

def updateTarget(op_holder, sess):
    for op in op_holder:
        sess.run(op)
        
class RLDispatchingServer():
    NUM_RESOURCE = 1770
    NUM_TIMESLOT = 100
    NUM_TRAIN = 1 # already summed up in build_state_actions
    
    d = 0.99 # discount
    tau = 0.001 #Rate to update target network toward primary network
    batch_size = 8
    epoch_buffer_size = 50
    
    update_frequency = 5
    train_step = 0
    sess = None
    
    is_initialized = False
    
    clientserver = None
    
    def __init__(self):
        self.mainQN = QNetwork(self.NUM_RESOURCE, self.NUM_TIMESLOT, self.NUM_TRAIN)
        self.targetQN = QNetwork(self.NUM_RESOURCE, self.NUM_TIMESLOT, self.NUM_TRAIN)
        
        trainables = tf.trainable_variables()
        self.targetOps = updateTargetGraph(trainables, self.tau)
        
        self.myBuffer = ExperienceBuffer()
        self.epochBuffer = ExperienceBuffer()
        
        self.sess = tf.InteractiveSession()
        tf.global_variables_initializer().run()
        
        self.tensor_board_writer = tf.summary.FileWriter('training', self.sess.graph)
        
        self.is_initialized = True

    def isInitialized(self):
        return self.is_initialized
    
    # Java: List<List<int[]>> resource_list, List<List<float[]>> start_list, end_list>, a list of inputs for state_actions
    # return the resulted state caused by the given state and action
    def build_state_actions(self, resource_list, start_list, end_list):
        state_actions = []
        b_size = len(resource_list) # batch size of the input

        for b_idx in range (0, b_size): # b_idx: index of state action in a batch
            # to build one state_action
            r_array_list = resource_list[b_idx] #List<int[]>
            s_array_list = start_list[b_idx] #List<float[]>
            e_array_list = end_list[b_idx] #List<float[]>
            
            s_matrix_sum = self.build_single_state_action(r_array_list, s_array_list, e_array_list)
            state_actions.append(s_matrix_sum)        
                        
        return state_actions
    
    def build_single_state_action(self, r_array_list, s_array_list, e_array_list):
        minVal = min([min(v) for v in s_array_list])
        maxVal = max([max(v) for v in e_array_list])
            
        num_train = len(r_array_list)
            
        s_matrix = np.zeros((self.NUM_RESOURCE, self.NUM_TIMESLOT, num_train))
            
        for t_idx in range (0, num_train): # t_idx: index of train
            r_array =  r_array_list[t_idx]
            s_array = s_array_list[t_idx]
            e_array = e_array_list[t_idx]
            length = len(r_array)
                
            for idx in range (0, length):
                r_idx = r_array[idx]
                s_idx = math.floor((s_array[idx] - minVal)/(maxVal - minVal) * 100)
                e_idx = math.ceil((e_array[idx]) - minVal/(maxVal - minVal) * 100)
                   
                s_matrix[r_idx, s_idx : e_idx+1, t_idx] = 1
            
        s_matrix_sum = np.reshape(s_matrix.sum(axis=2), (self.NUM_RESOURCE, self.NUM_TIMESLOT, 1)) # sum up and reshape
        return s_matrix_sum

    # internal call with give state_actions in the form of matrix
    def calculate_Q_vlaues(self, state_actions, fromMainQN = True):
        q_values = None
        if fromMainQN :
            q_values = self.sess.run(self.mainQN.predict, feed_dict={self.mainQN.state_ph: state_actions})
        else:
            q_values = self.sess.run(self.targetQN.predict, feed_dict={self.targetQN.state_ph: state_actions})
        return q_values

    # Java: List<List<int[]>> resource_list, List<List<float[]>> start_list, end_list>, a list of inputs for state_actions
    # calculate a set of q values from give state action pairs
    def calculateQVlaues(self, resource_list, start_list, end_list, fromMainQN = True):
        gc.disable()
        
        #logger = logging.getLogger('py4j')
        #logger.info("calculate q values ...")
        state_actions = self.build_state_actions(resource_list, start_list, end_list)
        q_values = self.calculate_Q_vlaues(state_actions, fromMainQN)
        #logger.info(q_values)
        
        gc.collect()
        return ListConverter().convert(q_values.tolist(), self.clientserver._gateway_client)
    
    # Java call through Py4j
    # state_action_aslist: List[ ResourceIndexList: List<int[], StartList: List<float[], EndList: List<float[] ]
    # reward: double
    # next_state_actions_aslist List [ state_action_aslist ]
    def addExperience(self, state_action_aslist, reward, next_state_actions_aslist):
        gc.disable()
        
        state_action = self.build_state_actions([state_action_aslist[0]],
                                                [state_action_aslist[1]],
                                                [state_action_aslist[2]])[0]
        next_state_actions = []
        for idx in range (len(next_state_actions_aslist)):
            entry = list(next_state_actions_aslist)[idx]
            next_state_action = self.build_single_state_action(entry[0], entry[1], entry[2])
            next_state_actions.append(next_state_action)
            
        experience = Experience(state_action, reward, next_state_actions)
        self.epochBuffer.buffer.append(experience)
        if len(self.epochBuffer.buffer) >= self.epoch_buffer_size :
            self.myBuffer.extend_experiences(self.epochBuffer.buffer)
            self.epochBuffer.buffer.clear()
        
        gc.collect()
    
    def doTraining(self):
        gc.disable()
         
        trainBatch = self.myBuffer.sample(self.batch_size) # the state_action in an experience has been build as matrix
        
        state_actions = []
        rewards = []
        next_all_state_actions = []
        next_actions_size = []
        
        for batch_index in range(self.batch_size):
            state_actions.append(trainBatch[batch_index].state_action)
            rewards.append(trainBatch[batch_index].reward)
            next_all_state_actions.extend(trainBatch[batch_index].next_state_actions)
            next_actions_size.append(len(trainBatch[batch_index].next_state_actions))
        
        all_q_next = self.calculate_Q_vlaues(next_all_state_actions, False)
        targetQ = []
        currentIdx = 0
        
        for batch_index in range(self.batch_size):
            min_q = min(all_q_next[currentIdx : currentIdx + next_actions_size[batch_index]])
            # TODO: check the possibility of using Double DQN or Dueling DQN
            targetQ.append(rewards[batch_index] + self.d * min_q)
            currentIdx += next_actions_size[batch_index]
        
        _, loss, summary  = self.sess.run([self.mainQN.update, self.mainQN.loss, self.mainQN.summary_op], 
                                feed_dict={self.mainQN.state_ph: state_actions, 
                                           self.mainQN.targetQ: targetQ})
        
        self.tensor_board_writer.add_summary(summary, self.train_step)
        
        self.train_step += 1
        if self.train_step % self.update_frequency == 0 :
            updateTarget(self.targetOps, self.sess) #Update the target network toward the primary network.
        
        gc.collect()
        return loss.item()
    
    def doTest(self, arg):
        return arg
    
    # Only for information. Can be deleted.
    class Java:
        implements = ["railapp.simulation.listeners.IRLListener"]

# Startup for initialization

from py4j.clientserver import ClientServer, JavaParameters, PythonParameters

python_log=True

if python_log:
    logging.basicConfig(filename='error.log', level=logging.ERROR)
    logger = logging.getLogger("py4j")
    logger.addHandler(logging.StreamHandler())
    
print("Create RLDispatchingServer...")

dispatching_server = RLDispatchingServer()

print("Initialize ClientServer...")

clientserver = ClientServer(
    java_parameters=JavaParameters(),
    python_parameters=PythonParameters(),
    python_server_entry_point=dispatching_server)

# set ClientServer for converting to java object
dispatching_server.clientserver = clientserver

print("Initialization of ClientServer accomplished")
