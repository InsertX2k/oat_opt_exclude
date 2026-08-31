# ---------------------------------------------------------------------
# ReadVolumeKeys - read the status of the volume keys
#
# Usage: ReadVolumeKeys [timeout] 
#
# Parameter:
#        timeout - timeout in seconds to wait; the default value is 10 seconds
#
# returns:
#        0 - no volume key pressed 
#        1 - volume up pressed
#        2 - volume down pressed
#        3 - power key pressed
#
function ReadVolumeKeys {
  local __FUNCTION="ReadVolumeKeys"

  local THISRC=0
   
  local DEFAULT_TIMEOUT=10
  
  local TIMEOUT="${DEFAULT_TIMEOUT}"
  local MESSAGE=""
  
  local dev=""
  local type=""
  local code=""
  local value=""
  
  local RESULT=""

  if [ $# -eq 1 ] ; then
    TIMEOUT=$1
    shift
  fi

# the variables dev, type, code, and value can not be used here because the "while" statement is running a separate session
#
  RESULT=$( timeout ${TIMEOUT} getevent -ql | while read dev type code value; do
    if [ "$code" = "KEY_VOLUMEUP" ] && [ "$value" = "DOWN" ]; then
      echo 1
      break
    elif [ "$code" = "KEY_VOLUMEDOWN" ] && [ "$value" = "DOWN" ]; then
      echo 2
      break
    elif  [ "$code" = "KEY_POWER" ] && [ "$value" = "DOWN" ]; then

# activate the display again (no pin necessary here -- at least in my tests ....)
# 	    
      input keyevent KEYCODE_WAKEUP
      echo 3
      break
    fi
  done )
   
  [ "${RESULT}"x != ""x ] && THISRC="${RESULT}"
  
  return ${THISRC}
}
echo "****************************************"
echo "This will clear system apps', and user apps' precompiled OATs"
echo "****************************************"
echo "Do you want to continue?, Press the volume up button for Yes, otherwise (close this window or press any other button for No)"
ReadVolumeKeys
if [ $? -eq 1  ] ; then
    echo "Starting Operation..."
else
    echo "Operation canceled by user!"
    exit
fi

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