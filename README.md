# Halo
Yet Another HomeAssistant App

##Proof of concept##  HomeAssistant Android app with Device tracker functionality through MQTT(Like Zanzito)
##No native UI Only Lovelace##
This is basically a remix of https://github.com/sjvc/Home-Assistant-Launcher and Google Location Examples https://github.com/android/location-samples  

Needs MQTT available from outside for device tracker to work, SSL not supported yet

To setup device tracker

add 
```
- platform: mqtt_json
  devices:
      pix3halo: halo/<device_name>/location 
```      
  to configuration.yaml
  

![Settings](https://user-images.githubusercontent.com/35991/69001969-822a1700-0933-11ea-9174-22823176c27d.png) ![Screenshot_1573955786](https://user-images.githubusercontent.com/35991/69001971-86eecb00-0933-11ea-8d35-52e533afaaf1.png)

Swipe left to access App Settings

![Screenshot_1573955890](https://user-images.githubusercontent.com/35991/69001973-8ce4ac00-0933-11ea-9cd8-58f424359021.png)
![Screenshot_1573955932](https://user-images.githubusercontent.com/35991/69001975-9110c980-0933-11ea-8977-3013e2904ba7.png)

Swipe Right to access HA Settings
