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


## Proposed Method & Expected Results

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


## Related works
- [A Smartphone Based Hand-Held Indoor Positioning System](https://www.researchgate.net/publication/301529181_A_Smartphone_Based_Hand-Held_Indoor_Positioning_System) - This paper uses IMU data for indoor positioning and tracks a person's walking path. I can use this paper by implementing the algorithms explained in this paper for map generation. 
- [An Indoor Position-Estimation Algorithm Using Smartphone IMU Sensor Data](https://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=8606925) - This paper provides formulas and explanations on how to use a phone's IMU for tracking indoor position. I can use this paper by implementing the algorithms explained in this paper for map generation.  
- [An iBeacon Primer for Indoor Localization](https://dl.acm.org/doi/10.1145/2674061.2675028) - This paper shows how BLE beacons can be used for indoor localization. I can use this paper by implementing the algorithms explained in this paper for indoor positioning. 


## Progress

I developed an Android app with an activity that sends BLE RSSI data through MQTT, both in app foreground & background. Currently, I am working on collecting and sending IMU data through MQTT. 

![Progress_1](https://github.com/maxgong21/ecem202a-project/blob/main/progress_1.jpg)
