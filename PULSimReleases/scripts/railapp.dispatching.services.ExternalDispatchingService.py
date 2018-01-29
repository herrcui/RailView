from py4j.java_collections import SetConverter, MapConverter, ListConverter
from py4j.clientserver import ClientServer, JavaParameters, PythonParameters
import logging
import random

class RandomDispatcher():
    is_initialized = False
    simulator = None
    
    def __init__(self):
        self.is_initialized = True
    
    def isInitialized(self):
        return self.is_initialized
        
    def determineAction(self, actions, conflictTrains):
        # add your own logic here
        
        # example for with FCFS (probability 50%) or give the priority for the train with the maximum delays
        if random.uniform(0, 1) < 0.5: 
            return 0
        
        # example for return the train with the maximum delays
        allTrains = self.simulator.getTrainSimulators()
        index = 0
        maxDelaySeconds = 0
        for i in range(len(conflictTrains)):
            delay = conflictTrains[i].getDelay()
            delaySeconds = 0
            if delay != None:
                delaySeconds = delay.getTotalSecond()
                
            if delaySeconds > maxDelaySeconds:
                maxDelaySeconds = delaySeconds
                index = i
        return index
        
    def setSimulator(self, simulator):
        self.simulator = simulator

python_log=True

if python_log:
    logging.basicConfig(filename='error.log', level=logging.ERROR)
    logger = logging.getLogger("py4j")
    logger.addHandler(logging.StreamHandler())
    
dispatching_server = RandomDispatcher()

clientserver = ClientServer(
    java_parameters=JavaParameters(),
    python_parameters=PythonParameters(),
    python_server_entry_point=dispatching_server)
    
dispatching_server.clientserver = clientserver
