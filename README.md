# Auto-Generate Map of BLE Beacons for Indoor positioning Using a Phone's Motion Sensors

UCLA ECE M202A Final Project by Max Gong, Fall 2020

## Abstract

Bluetooth Low Energy (BLE) beacons for indoor positioning have been studied closely in the past decade and have even developed into a marketable technology, as BLE is a cheap indoor positioning solution that is valuable in many industry verticals such as warehousing and construction. A typical use case is using BLE beacons to track the location of a person’s phone. However, there are little to no solutions for locating the position of the BLE beacons themselves. Typically, once beacons are placed in their spot, the locations of the beacon have to be manually entered. This can result in inaccuracies and presents a major hassle for the user. In this project, I use an Android phone’s motion sensors to locate the position of beacons and generate a map of beacons. I then demonstrate BLE for indoor positioning with the auto-generated map of beacon positions. 

## Motivation

Indoor positioning brings much value in many industry verticals. In warehouses, being able to navigate to an item or to a shelf can help save money and time. In hospitals, knowing the locations of equipment can save time and even save lives.

Bluetooth Low Energy (BLE) beacons have been studied closely in the past decade as a cheap solution to indoor positioning. They have an accuracy range of about 1-2 meters, which is perfect for many office, warehouse, and large building settings. 

***However, there are little to no solutions for locating the beacons.*** Once beacons are placed in their spot, the user will have to manually enter in the location of the beacons, which may not be accurate. 

***What if there was a way to auto-map beacons once you set them down in their spots? What if you could generate a map of your beacons only with your phone?*** 


## Idea

**The main idea: _Auto-generate map of beacons for indoor positioning using a phone's motion sensors_** 

There are 2 parts to my idea: 
1. ***Mapping***: I aim to use a phone's motion sensors to locate and generate a map of beacons.
2. ***Indoor positioning***: Once the map is created, I aim to use these beacons to locate the position of a phone and display that location on a 2D eagle-eye view map.

An example use case of this: A warehouse manager downloads the app. They use the app to locate and generate a map of beacons. Then, warehouse employees download the app. They activate the app when they come to work, so that their positions can be tracked on by the warehouse manager. 


## Proposed Method and Expected Results

#### Here are the materials I plan to use. 
- Android phone (Samsung Galaxy S9)
- BLE beacons (BlueCharm beacons)
- PC 

#### These are the languages I plan to use. 
- Android Java, for developing the Android app and collecting IMU & BLE RSSI data. 
- Python, for processing IMU & BLE RSSI data to generate map & indoor positioning.

#### Here is my proposed flow for Mapping. 
1. User starts from one beacon to begin mapping.  
2. Android phone app sends IMU & BLE RSSI data to PC, using MQTT protocol. 
3. Python program on PC will process IMU & BLE RSSI data to generate map. 
4. Map is finished when user reaches last beacon and stops mapping. 
5. Map is generated in Python & ready to use for indoor positioning. 

#### Here is my proposed flow for Indoor Positioning.
1. User activates BLE monitoring on Android phone app.
2. In the background, Android phone app sends BLE RSSI data to PC, using MQTT protocol. 
3. Python program on PC uses triangulation with BLE RSSI data to locate position of phone. 
4. Map displays the position of phone in relation to beacon positions.  

I think the most challenging and innovative part is meshing the IMU & BLE data to map the beacons' positions. 


## Prior Works
BLE beacons have gained recent attention because they provide a low-cost indoor positioning solution that can offer an average position estimation error of 0.53 meters [1]. Many prior works focus on improving the accuracy of indoor positioning when using BLE beacons. Paper [1] presents an indoor positioning solution using Apple’s iBeacon protocol and weighted calculations of RSSI readings. Paper [2] presents an improved bluetooth beacon-based indoor positioning method that uses a combination of weighted distance estimations and their novel distance pondering algorithm, instead of the classic triangulation technique. This results in a 13.2% improvement in location accuracy, resulting in a position estimation error of less than 1.5 meters. Position estimation errors of 0.53 to 1.5 meters are accurate enough for indoor positioning in large spaces such as warehouses, given that the position of the beacons are known. 

There are only a few prior works that focus on locating BLE beacons using RSSI, but their reported location accuracy is relatively low. Paper [3] uses on-line calibration on a set of beacons and a fusion of BLE RSSI and a phone’s motion sensors to locate the position of BLE beacons relative to the phone’s position. They achieved a location error of 1.2 to 1.8 meters, which may not be accurate enough to generate a useable and accurate map of beacons. It is difficult to use BLE beacon for indoor positioning when the position of the beacons themselves are inaccurate. 

Tracing a person’s walking path using a phone’s IMU has shown to be accurate, and many works have been published on using IMU for path tracing. Paper [4] presents a smartphone-based hand-held indoor positioning system that only uses the phone’s accelerometer, gyroscope, and gravity virtual sensors. The paper uses Pythagorean Theorem to calculate step length, gyroscope readings to estimate angle direction, and Kalman-filtered accelerometer readings to estimate position. The paper was able to generate a 3D map of the person’s walking trail, with an estimation error of 1.5%. Paper [5] also uses very similar techniques as paper [4], along with magnetometer readings and a pitch-based step detection algorithm. This paper was able to generate a 2D map of the person’s walking trail, with an estimation error of 2.5%. 

Although IMU has shown to provide high path tracing accuracy, I have only found one work that uses IMU to locate the position of beacons. Paper [6] uses graph-based SLAM techniques to process IMU and BLE RSSI readings to determine the position of beacons. This paper was able to achieve a beacon position estimation error of 1.27 meters, which is still relatively high. I aim to combine user input with a variation of the technique in this paper to improve beacon position accuracy. 


## Timeline

| Time | Action Item |
| :---: | :--- |
| Week 1 | Form idea. |
| Week 2 | Test idea & discuss with professor and TA. |
| Week 3 | Develop Android phone app to read BLE RSSI and communicate it using MQTT. |
| Week 4 | Create Github website. Develop Python code to read BLE RSSI data sent from Android phone. |
| Week 5 | Develop Android activity to read IMU data and communicate it using MQTT. |
| Week 6 | Implement graphing & mapping of BLE beacons on Python. |
| Week 7 | Implement graphing & mapping of BLE beacons on Python. |
| Week 8 | Polish graphing & mapping of BLE beacons on Python. Retrieve metrics of improved accuracy. |
| Week 9 | Implement BLE positioning on Python & generate real-time 2D map. |
| Week 10 | Implement BLE positioning on Python & generate real-time 2D map. |


## Progress

I developed an Android app with an activity that sends BLE RSSI data through MQTT, both in app foreground & background. Currently, I am working on collecting and sending IMU data through MQTT. 

![Progress_1](https://github.com/maxgong21/ecem202a-project/blob/main/progress_1.jpg)


## References

[1] [An iBeacon Primer for Indoor Localization](https://dl.acm.org/doi/10.1145/2674061.2675028) 

[2] [Improved bluetooth beacon-based indoor location and fingerprinting](https://doi.org/10.1007/s12652-019-01626-2) 

[3] [Locating and Tracking BLE Beacons with Smartphones](https://dl.acm.org/doi/10.1145/3143361.3143385)

[4] [A Smartphone Based Hand-Held Indoor Positioning System](https://www.researchgate.net/publication/301529181_A_Smartphone_Based_Hand-Held_Indoor_Positioning_System) 

[5] [An Indoor Position-Estimation Algorithm Using Smartphone IMU Sensor Data](https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=8606925)

[6] [Indoor Positioning Based on Bluetooth Low Energy Beacons Adopting Graph Optimization](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6264008/)

