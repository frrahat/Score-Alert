####Score Alert
Simple cric score alerter

###Help:
Steps:\n
        1. Use \"Prev\" or \"Next\" button to navigate to the xml line you want to track\n
        2. Go to options, select \"Lock This Line\" to lock the highlighted line\n
        3. Go to top of the screen, you will see the tracking attributes\n
        4. Click on the text of the top, a dialog will open\n
        5. Input the attributes separated by commas in that dialog\n
        6. To play alert tone you need to set alert condition to attribute\n
        7. To set alert condition place the condition string (Example is given below) with each attribue separated by a \'=\' without the quotes\n
       	\n
       	\n
    	Alert Condition Example:\n
    	r=200,wkts=+1\n
    	==> Alert tone will be played for each increment of wkts and for value of r hitting 200\n
    	\n
    	r=+4,wkts=+1\n
    	==> Alert tone will be played for each increment of wkts and every 4 or greater value change of r (you may use this to set alert for boundary)\n
    	\n
    	\n
    	Refresh Delay:\n
    	Refresh delay is set to 30s when the app starts. You can reduce it up to 5s. Setting less than that will turn off refreshing.\n
    	\n
    	\n
    	For more help, please contact: fr.rahat@gmail.com