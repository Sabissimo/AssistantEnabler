# Assistant Enabler

Xposed module: Enable Google Assistant in Marshmallow ROMs

Enable/disable Google Assistant without restart
Bonus feature: Enable Google Now in unsupported countries*

## How to install

Simply grab the module from [the Xposed Module Repository](http://repo.xposed.info/module/com.sabik.assistantenabler).

To make a build build on your own device from source, feel free to clone this repository and use Gradle for that. You can read more about how to build locally on [Android Developers](https://developer.android.com/tools/building/building-cmdline.html) or just Google around.


#To use "OK Google" hotword you need to do following:
1. Disable assistant in module
2. Configure "OK Google" detection and voice pattern
3. Enable assistant in module
4. Say "OK Google"
5. ....
6. Profit!

# In order to enable Google Now in unsupported country, these steps are required:
* 1) Enable Assistant Enabler module in Xposed Installer
* 2) In Assistant Enabler settings, enable "Enable Google Now"
* 3) Reboot (after rebooting your phone carrier will be identified as US Verizon)
* 4) Either remove your sim, or put your phone in airplane mode
* 5) Enable Wifi
* 6) If google account is added to the phone already - remove it
* 7) Launch google app and login to your google account
* 8) Opt-in to Google Now
* 9) PROFIT!

P.S. After these steps you can disable "Enable Google Now" setting in Assistant Enabler module, if you don't want your carrier to be identified as VZW. Google Now will continue working.