# HackTheNortheast
Tempest - Evan Chang, Lucas Balangero, and Omar Nunez's hack for Hack the Northeast! We decided to make a weather app designed for runners, by runners. It allows users to set their exercise preferrences and then view a schedule created to help them exercise throughout the week.

Sources:
Our app is built using the DarkSky weather API, a service that provides weather data for any given location. We used a fusedLocationProvider from Android to get the devices data, which gave us coordinates for DarkSky. We also parsed the JSON object returned by DarkSky with the public OkHttp library.
