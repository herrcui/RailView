package ikr.simlab.demo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import railapp.activemq.TopicMessageCommunicator;
import railapp.activemq.messages.dispatching.ForwardMessage;
import railapp.activemq.messages.dispatching.MessageTopics;
import railapp.activemq.messages.dispatching.TopicMessage;

public class CommunicatorDemo extends TopicMessageCommunicator {
    private int senderid = 100;
    
    public static CommunicatorDemo getInstance(String name) {
        ArrayList<String> topics = new ArrayList<String>();
        // add subscribed topics send from PULSim here.
        topics.add(MessageTopics.SIRO_OCCUPANCY.toString());
        topics.add(MessageTopics.SIRO_DISPATCHING.toString());
        
        return new CommunicatorDemo(name, topics);
    }
    
    private CommunicatorDemo(String name, List<String> topics) {
        super(name, topics);
    }
    
    @Override
    protected void consumeObjectMessage(ObjectMessage objMessage) {
        try {
            Object obj = objMessage.getObject();
            if (obj instanceof TopicMessage) {
               TopicMessage topicMsg = (TopicMessage) obj;
               // TODO: set IKR simulation time
               long stamp = (new Date()).getTime();
               System.out.println(topicMsg);
               
               switch (topicMsg.getTopic()) {
                   case SIRO_OCCUPANCY:
                       this.sendMessage(new ForwardMessage(
                               this.senderid, 
                               MessageTopics.IKR_OCCUPANCY,
                               stamp,
                               topicMsg));
                       break;
                   case SIRO_DISPATCHING:
                       this.sendMessage(new ForwardMessage(
                               this.senderid, 
                               MessageTopics.IKR_DISPATCHING,
                               stamp,
                               topicMsg));
                       break;
                   default:
                       System.out.println("Unknowen Message");
                       break;
                }                
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws JMSException { 
        CommunicatorDemo demo = CommunicatorDemo.getInstance("IKR_CommDemo");
        demo.start();
    }
}
