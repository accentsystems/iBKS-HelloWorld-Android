<span id="_Toc456607170" class="anchor"><span id="_Toc456859050" class="anchor"></span></span>iBKS SDK for Android
==================================================================================================================

**ABSTRACT**

The demo App **iBKS Hello World** is a very useful sample of the most important functions that can be used to detect and interact with a Bluetooth device. In this document is explained how this project, implemented for Android Studio, is structured and what are the functions that can be found on it.

**AUDIENCE**

The **iBKS Hello World** is primarily focused for Android software developers with basic knowledge of beacon configuration


<span id="_Toc456607170" class="anchor"><span id="_Toc456859050" class="anchor"></span></span>Project iBKS Hello World
==============================================================================================================

After download de “iBKS Hello World” project, you only have to open it on Android studio and compile it. All the needed libraries are included as dependencies and downloaded automatically.

The project is structured to show three important functionalities, each one on a different class:
- **ScanActivity**: scans and list the beacons that are advertising around and allows discovering services and characteristics.
- **NotificationDemo**: Show a notification dialog on App triggered by a specific beacon packet detected.
- **BackgroundScan**: Starts background scan that allows to detect beacons and do some actions (send notification, open app, ...) even when the app is stopped.

The first activity started on foreground is **MainActivity** that show the different options of the app and check the app permissions.

<span id="_Toc456607171" class="anchor"><span id="_Toc456859051" class="anchor"></span></span>App permissions
========================================================================================================

To manage Bluetooth in Android it’s necessary to request some permissions at user.


<span id="_Toc456607172" class="anchor"><span id="_Toc456859052" class="anchor"></span></span> Location 
------------------------------------------------------------------------------------------------------------------

<span id="_Toc456607173" class="anchor"></span>If the Android version is 6.0 or higher it’s necessary to request location permission.
To do this it’s necessary to add permission in AndroidManifest.xml

``<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />``                    
``<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />``

The function in charge to check location permission is checkLocBT()in MainActivity.


Bluetooth 
---------------------------------------------------------------------------------------------------

To use Bluetooth in Android device, the first thing to do is check if the device that
runs the app has Bluetooth Low Energy (beacons work with this type of Bluetooth)
and if it is enabled. To enable Bluetooth it’s necessary to add permission in
AndroidManifest.xml

``<uses-permission android:name="android.permission.BLUETOOTH" />``         
``<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />``

The function in charge to do this check is inicializeBluetooth()in MainActivity.


<span id="_Toc456607171" class="anchor"><span id="_Toc456859051" class="anchor"></span></span>
========================================================================================================
Last update: 2016/07/26
