echo "Clearing pre-compiled VDEX, and ODEXs for system packages (apps in /system/app, and /system/priv-app)..."
rm -rfv /data/dalvik-cache/*
echo "Cleared /data/dalvik-cache !"
echo "Clearing AOT pre-builts for user apps..."
find /data/app -type d -name oat -exec rm -rfv {} +
echo "Cleared pre-built AOTs for user apps!"
echo "****************************************"
echo "- You may now want to reboot your device into recovery mode then select the option Repair Apps if you are using Samsung stock recovery to rebuild AOTs for system apps"
echo "= Or if you are not running Samsung stock recovery/Android stock recovery you may just want to reboot normally."
echo "****************************************"
echo "WARNING: NOT REBOOTING AND TRYING TO OPEN ANY APP AFTER A SYSTEM PRE-COMPILED VDEX, ODEX PURGE WILL CAUSE APP CRASHES!!!"