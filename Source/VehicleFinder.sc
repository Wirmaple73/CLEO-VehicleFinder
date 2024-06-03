SCRIPT_START
{
	CONST_INT BLIP_COLOR 0x9361DBFF
	CONST_INT MAXIMUM_TRIES 200
	CONST_FLOAT VEHICLE_SEARCH_RANGE 8192.0f

	LVAR_INT finderKey1, finderKey2, selectionKey, speedupKey, incrementationKey, decrementationKey  // Keybinds

	LVAR_INT player, selectedModel, foundVehicle, blip, numTries
	LVAR_FLOAT playerCoordinates[3]
	LVAR_TEXT_LABEL modelName

	selectedModel = 400
	GET_PLAYER_CHAR 0, player

	GOSUB ReadKeybinds

	WHILE TRUE
		WAIT 0

		IF NOT IS_CAR_DEAD foundVehicle
		AND DOES_BLIP_EXIST blip
			GOSUB UnmarkVehicle
		ENDIF

		IF IS_KEY_PRESSED finderKey1
		AND IS_KEY_PRESSED finderKey2
			GOSUB Main
		ENDIF

		IF IS_KEY_PRESSED selectionKey
			GOSUB HandleModelSelection
		ENDIF
	ENDWHILE

  Main:
	GET_CHAR_COORDINATES player, (playerCoordinates[0], playerCoordinates[1], playerCoordinates[2])
	REMOVE_BLIP blip
	numTries = 0

	WHILE numTries < MAXIMUM_TRIES
		GET_RANDOM_CAR_IN_SPHERE_NO_SAVE_RECURSIVE (playerCoordinates[0], playerCoordinates[1], playerCoordinates[2]), VEHICLE_SEARCH_RANGE, TRUE, TRUE, foundVehicle

		IF IS_CAR_DEAD foundVehicle  // Ensure the car's handle is valid
			CONTINUE
		ENDIF

		++numTries

		IF IS_CAR_MODEL foundVehicle, selectedModel
			ADD_BLIP_FOR_CAR foundVehicle, blip
			CHANGE_BLIP_COLOUR blip, BLIP_COLOR

			PRINT_FORMATTED_NOW "The first matching ~p~vehicle~s~ has been marked on the radar.", 2000
			RETURN
		ENDIF
	ENDWHILE

	PRINT_FORMATTED_NOW "Could not find any vehicles with ID ~y~%i~s~ within a reasonable distance.", 2000, selectedModel
	RETURN
	
  HandleModelSelection:
	IF NOT IS_KEY_PRESSED incrementationKey
	AND NOT IS_KEY_PRESSED decrementationKey
		RETURN
	ENDIF

	IF IS_KEY_PRESSED incrementationKey
		++selectedModel
	ENDIF

	IF IS_KEY_PRESSED decrementationKey
		--selectedModel
	ENDIF

	IF selectedModel < 400  // Wrap around valid model IDs (400-611)
		selectedModel = 611
	ENDIF

	IF selectedModel > 611
		selectedModel = 400
	ENDIF

	GET_NAME_OF_VEHICLE_MODEL selectedModel, modelName
	PRINT_FORMATTED_NOW "Selected vehicle ID: ~y~%i~s~ (%s)", 2000, selectedModel, $modelName

	IF NOT IS_KEY_PRESSED speedupKey  // Selection cooldown
		WAIT 100
	ENDIF
	
	RETURN

  UnmarkVehicle:
	IF TEST_CHEAT "UNMARK"
	OR IS_CHAR_IN_CAR player, foundVehicle
		REMOVE_BLIP blip
		PRINT_FORMATTED_NOW "Vehicle has been unmarked.", 2000
	ENDIF

	RETURN

  ReadKeybinds:
	// GTA3Script apparently doesn't support passing strings as parameters to functions and constant strings yet
	IF NOT READ_INT_FROM_INI_FILE ("CLEO\VehicleFinder.ini", "VehicleFinder", "ActivationKey1"), finderKey1
		finderKey1 = VK_LCONTROL
	ENDIF

	IF NOT READ_INT_FROM_INI_FILE ("CLEO\VehicleFinder.ini", "VehicleFinder", "ActivationKey2"), finderKey2
		finderKey2 = VK_KEY_M
	ENDIF

	IF NOT READ_INT_FROM_INI_FILE ("CLEO\VehicleFinder.ini", "ModelSelection", "ActivationKey"), selectionKey
		selectionKey = VK_LCONTROL
	ENDIF

	IF NOT READ_INT_FROM_INI_FILE ("CLEO\VehicleFinder.ini", "ModelSelection", "SpeedupKey"), speedupKey
		speedupKey = VK_LSHIFT
	ENDIF

	IF NOT READ_INT_FROM_INI_FILE ("CLEO\VehicleFinder.ini", "ModelSelection", "IncrementationKey"), incrementationKey
		incrementationKey = VK_ADD
	ENDIF

	IF NOT READ_INT_FROM_INI_FILE ("CLEO\VehicleFinder.ini", "ModelSelection", "DecrementationKey"), decrementationKey
		decrementationKey = VK_SUBTRACT
	ENDIF

	RETURN
}
SCRIPT_END
