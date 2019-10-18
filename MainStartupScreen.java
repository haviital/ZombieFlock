import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.Sprite;
import femto.input.Button;
import femto.sound.Mixer;
import femto.sound.Procedural;
import femto.Image;
import Pico8Ext;

import Common;
import Event;

public class MainStartupScreen extends State 
{
    
    // Member data
     TitleBrew titleBrewImage;
     TitleCup titleCupImage;
     TitleHands titleHandsImage;
     TitleOfTheUndead titleOfTheUndeadImage;
     missozzy_zombie_02 soundEffect;
     long programStartTimeMs; // the start time of the program
     MenuDlg menuDlg;
     boolean doPrintBackStory;
     boolean isInfoScreenActive;
     int arrCount = 3;
     String textLineArray[];
     
    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init(){

        Main.screen.loadPalette(Pico8Ext.palette());

        Main.screen.cameraX = 0;
        Main.screen.cameraY = 0;
        
        // minus 1 so that the Common.currentFrameStartTimeMs do not start from 0.
        programStartTimeMs = System.currentTimeMillis() - 1; 
        Common.currentFrameStartTimeMs = System.currentTimeMillis() - programStartTimeMs;
        
        //
        titleBrewImage =  new TitleBrew();
        titleCupImage =  new TitleCup();
        titleHandsImage =  new TitleHands();
        titleOfTheUndeadImage =  new TitleOfTheUndead();
        
        soundEffect = new missozzy_zombie_02(0);

        // Create events.
        Common.events = new Event[Common.MAX_EVENTS]; //
        for(int i=0; i < Common.MAX_EVENTS; i++) Common.events[i] = new Event();
        
        // Create image animation events.
        Common.events[0].setMoveAnimEvent(3*1000, 8*1000,  0,  70,  0,  4, titleHandsImage, null, false, 2, 2 );
        Common.events[1].setGfxPrimitiveMoveAnimEvent(0, 999*1000,  0, 70, 0, 70,  
                Event.GFX_PRIMITIVE_RECT, 220, 176 , 7, false );
        Common.events[2].setMoveAnimEvent(0, 2*1000,  -titleCupImage.width(), 70,  2, 70, titleCupImage, null, false, 0, 0 );
        Common.events[3].setMoveAnimEvent(0, 2*1000, 220, 78, 76, 78, titleBrewImage, null, false, 0, 0 );
        Common.events[4].setMoveAnimEvent(2*1000, 3*1000,  2,176,  2,135, titleOfTheUndeadImage, null, false, 0, 0 );
        
        Common.events[5].setSfxEvent( 4*1000, soundEffect, 0, 0 ); 

        arrCount = 3;
        textLineArray[] = new String[arrCount];
        textLineArray[0] = "Play";
        textLineArray[1] = "Tutorial";
        textLineArray[2] = "Info";
    }
    
    // 
    void shutdown()
    {
        //
        Common.freeResources();
    }
    
    // update is called by femto.Game every frame
    void update(){
        
        // Read the current time. Substract the program start time to make the value smaller, so it fits to 32 bits. 
        Common.currentFrameStartTimeMs = System.currentTimeMillis() - programStartTimeMs;

        // Put this first
        if( menuDlg != null )
            menuDlg.update();

        //System.out.print("menuDlg="+menuDlg+", menuDlg.pressedA="+menuDlg.pressedA);

         if( Button.B.justPressed() ) //!!!HV
         {
             // 
             Common.currentDay = 2;
             Common.totalBeanCount = 40*5;
             Common.totalCoffeeCount = 5;
             Game.changeState( new Main() );
         }
        
        // Pressed A in the back story window?
        boolean justPressedA = Button.A.justPressed();
        if( justPressedA && Common.isTutorialActive && doPrintBackStory == true )
        {
            // Start playing in the tutorial mode
            Common.currentDay = 0;
            Common.totalBeanCount = 0;
            Common.totalCoffeeCount = 0;
            Game.changeState( new MainDay() );
        }
        
        // Pressed A in the info window?
        else if( justPressedA && isInfoScreenActive == true )
        {
            // 
            isInfoScreenActive = false;
            // Create menu
            menuDlg = new MenuDlg( 45, 40, 220-90, 4*7 + 20, textLineArray );
        }
        
        // Change to a new state when A is pressed
        else if( ( menuDlg == null  && justPressedA ) ||
            ( menuDlg != null  && menuDlg.pressedA && menuDlg.focusIndex == 0 ) // Selected "Play" in the menu
        )
        {
            System.out.print("Main.eepromCookie.flags="+Main.eepromCookie.flags);
            if((Main.eepromCookie.flags & Main.eepromCookie.FLAG_TUTORIAL_PASSED) == 0)
            {
                String textLineArray1[] = new String[1];
                String textLineArray2[] = new String[1];
                textLineArray1[0] = "Please, pass the";
                textLineArray2[0] = "tutorial first!";
                Common.events[Main.getNextFreeEvent()].setTutorialBubbleEvent(0, textLineArray1, textLineArray2, 1 );
            }
            else
            {
                // Restart the game
                Common.totalBeanCount = 0;
                Common.totalCoffeeCount = 0;
                Game.changeState( new MainLevelMap() );
                
                //!!HV
                //Common.currentDay = 0;
                //Common.totalBeanCount = 120;
                //Common.totalCoffeeCount = 3;
                //Game.changeState( new Main() );
            }
        }
            
        else if( menuDlg != null  && menuDlg.pressedA ) // Selected a menu item
        {
            // Start tutorial
            if( menuDlg.focusIndex == 1 ) // Selected "Tutorial" in the menu
            {
                doPrintBackStory = true;
                Common.isTutorialActive = true;
                menuDlg = null;
            }
            
            // show info
            else if( menuDlg.focusIndex == 2 ) // Selected "Tutorial" in the menu
            {
                isInfoScreenActive = true;
                menuDlg = null;
            }
       }
            
                
         // 
        else if( Button.C.justPressed() )
        {
            // Create menu
            menuDlg = new MenuDlg( 45, 40, 220-90, 4*7 + 20, textLineArray );
        }
            
        // *** UPDATE

        // Events
        for(int i=0; i < Common.MAX_EVENTS; i++)
        {
            // Check if the event has occurred.
            if(!Common.events[i].isFree() && Common.currentFrameStartTimeMs > Common.events[i].eventTime)
            {
                // Store the event data before execute()
                long eventOccurred = Common.events[i].waitingForEvent;
                float x = Common.events[i].targetPosX;
                float y = Common.events[i].targetPosY;
                Image image = Common.events[i].image;
                
                //
                Common.events[i].execute();
                
                // If event was ended, start a static event which just draws the still image.
                if( eventOccurred  == Event.EVENT_END_MOVE_ANIM )
                {
                    // Still event, last forever.
                    Common.events[i].setMoveAnimEvent(0, 999*1000,  x,  y,  x,  y, image, null, false, 0, 0 );
                    Common.events[i].execute(); // Execute the start event immediately.
                }
                
            }
        }

        // *** DRAW
        
        if( doPrintBackStory )
        {
            Main.screen.clear( 14 );
            Main.drawBackStoryWindow(0); 
            Main.drawButtonAndLabel( 0, 176-14, 220, "A  Continue", "A");
        }
        else if( isInfoScreenActive )
        {
            Main.screen.clear( 14 );
            Main.drawBackStoryWindow(1); 
            Main.drawButtonAndLabel( 0, 176-14, 220, "A  Back", "A");
        }
        else
        {
            // Draw status pane background.
            Main.screen.fillRect(0,0, 220, 80, 11 );
            //Main.screen.fillRect(0,70, 220, 176, 8 );
    
            // Draw Common.events
            for(int i=0; i < Common.MAX_EVENTS; i++)
            {
                if(!Common.events[i].isFree() && Common.events[i].isDrawingState)
                    Common.events[i].draw(Main.screen);
            }
            
            //
            Main.drawButtonAndLabel( 0, 176-14, 220, "C  Menu", "C");
    
            // Draw menu if open
            if( menuDlg != null )
                menuDlg.draw();
        }
        
        // Update the screen with everything that was drawn
        Main.screen.flush();
    }
 
 
}


