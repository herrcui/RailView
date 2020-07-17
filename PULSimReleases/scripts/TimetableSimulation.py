from py4j.java_gateway import JavaGateway
import matplotlib.pyplot as plt

gateway = JavaGateway()

print ("load data ...")
# read infrastructure, rollingstocks and timetable data
timetable_entry = gateway.createTimetableSimulationEntry("d:\\temp\\python\\raildata\\infrastructure",
    "d:\\temp\\python\\raildata\\rollingstocks",
    "d:\\temp\\python\\raildata\\timetable")

simulator = timetable_entry.getSimulator()

print ("data loaded.")

# the simulation period will be set from 7:00 to 8:00

timetable_entry.setSimulationTime(simulator, 7, 8)

print("run simulation ...")
simulator.run()

# the results of simulation can be customized by user

def output_occupancy():
    print ("Occupancy time of tracks:")
    elements = timetable_entry.getAllInfrastructureElements()
    for element in elements:
        oMap = timetable_entry.getOccupancyMap(simulator, element)
        if oMap is not None:
            print ("Track Id: ", element, ":")
            lastKey = 0;
            for key in oMap:
                if key != 0:
                    print ("    ", lastKey, "-", key, "m:", oMap[key])
                lastKey = key
            print()

def output_train_arrive_departures():
    print ("Train arrive and departure time:")
    trains = timetable_entry.getAllTrains(simulator)
    index = 0
    if len(trains) > 0:
        # map: key - trip element, value - String[0] arrive String [1] departure 
        arrive_departures = timetable_entry.getArriveDepatures(simulator, index)
        if arrive_departures is not None:
            for key in arrive_departures:
                print(key,
                    ": arrive: ", arrive_departures[key][0],
                    " departure: ", arrive_departures[key][1])
    print()

def output_course():
    print ("Running dynamics:")
    trains = timetable_entry.getAllTrains(simulator)
    index = 0
    if len(trains) > 0:
        # List of points array: double[0] distance meter, [1] velocity km/h, [2] time second 
        course = timetable_entry.getCourse(simulator, index)
        if course is not None:
            distances = []
            velocities = []
            meter = 0
            for point in course:
                meter += point[0]
                print("distance [meter]: ", meter,
                        " velocity [km/h]: ", point[1],
                        " duration [seconds]: ", point[2])
                distances.append(meter);
                velocities.append(point[1])
    plt.plot(distances, velocities)
    plt.show()
    print()    

output_occupancy()
output_train_arrive_departures()
output_course()
