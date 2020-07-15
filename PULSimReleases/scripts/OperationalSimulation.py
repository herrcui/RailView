from py4j.java_gateway import JavaGateway
from py4j.java_collections import SetConverter, MapConverter, ListConverter
import matplotlib.pyplot as plt

gateway = JavaGateway()

print ("load data ...")
# read infrastructure, rollingstocks and timetable data
operation_entry = gateway.createOperationalSimulationEntry("d:\\temp\\python\\raildata\\infrastructure",
    "d:\\temp\\python\\raildata\\rollingstocks",
    "d:\\temp\\python\\raildata\\timetable")

print ("data loaded.")

# the simulation period will be set from 7:00 to 8:00

start_time = 25200
end_time = 28800

print("run timetable simulation without disturbances ...")

def print_delays(log, sim_trips, prefix=''):
    for trip in sim_trips:
        print(trip.getNumber())
        arrive_delays = log.getDelays(trip.getNumber(), 1)
        departure_delays = log.getDelays(trip.getNumber(), 2)
        print(prefix + ' s, '.join([str(elem.getTotalSeconds()) for elem in arrive_delays]))
        print(prefix + ' s, '.join([str(elem.getTotalSeconds()) for elem in departure_delays]))

# The original timetable is not conflict-free. 
# The delay_no_disturbances are the delays caused by the conflict without considering random disturbances.

trips = operation_entry.getUtilities().getTrips(start_time, end_time)
delay_no_disturbances = operation_entry.simulateWithoutDisturbances(start_time, end_time)
print_delays(delay_no_disturbances, trips, prefix="ts: ")

disturbances = []

# Supported distribution and parameters are distributions defined in:
#     org.apache.commons.math3.distribution.AbstractRealDistribution

# BetaDistribution("BetaDistribution"), // alpha, beta
# CauchyDistribution("CauchyDistribution"), // median, scale
# ChiSquaredDistribution("ChiSquaredDistribution"), // degreesOfFreedom
# ConstantRealDistribution("ConstantRealDistribution"), // value
# ExponentialDistribution("ExponentialDistribution"), // mean
# FDistribution("FDistribution"), // numeratorDegreesOfFreedom, denominatorDegreesOfFreedom
# GammaDistribution("GammaDistribution"), // shape, scale
# GumbelDistribution("GumbelDistribution"), // mu, beta
# LaplaceDistribution("LaplaceDistribution"), // mu, beta
# LevyDistribution("LevyDistribution"), // mu, c
# LogisticDistribution("LogisticDistribution"), // mu, s
# LogNormalDistribution("LogNormalDistribution"), // scale, shape
# NakagamiDistribution("NakagamiDistribution"), // mu, omega
# NormalDistribution("NormalDistribution"), // mean, sd
# ParetoDistribution("ParetoDistribution"), // scale, shape
# TDistribution("TDistribution"), // degreesOfFreedom
# TriangularDistribution("TriangularDistribution"), // a, c, b
# UniformRealDistribution("UniformRealDistribution"), // lower, upper
# WeibullDistribution("WeibullDistribution"); // alpha, beta

parameters = ListConverter().convert([15.0], gateway._gateway_client) # mean value of disturbance
distribution = operation_entry.createExpDist("ExponentialDistribution", parameters)
train_classes = operation_entry.getUtilities().getAllTrainClasses()
train_class_groups = operation_entry.buildTrainClassGroups(train_classes)

for train_class_group in train_class_groups:
    station = operation_entry.getAllStations()[0]
    disturbance = operation_entry.createDisturbanceDefinition(2, distribution, train_class_group, station)
    disturbances.append(disturbance)

dist_conv = ListConverter().convert(disturbances, gateway._gateway_client)

print("run operational simulation with disturbances ...")

size = 3
delays_with_disturbances = operation_entry.simulate(dist_conv, size, start_time, end_time)

# output the random disturbances
for round in range(size):
    disturnbances_single_sim = operation_entry.getDisturbances(round)
    for trip in trips:
        dist_map = disturnbances_single_sim.getDisturbanceMapByTrip(trip)
        if dist_map is not None:
            for type in dist_map:
                print(type)
                item = dist_map[type]
                print(', '.join([str(key) + ":" + str(item[key]) for key in item]))

# the results of simulation can be customized by user

round = 1
for delay_with_disturbances in delays_with_disturbances:
    print("Round: " + str(round))

    if delay_with_disturbances is not None:
        delay_log = delay_with_disturbances.getComparedDelayLogger(delay_no_disturbances)
    
        # print_delays(delay_with_disturbances, trips, prefix="os: ")
        print_delays(delay_log, trips, prefix="compare: ")

    round = round + 1
