import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.Sprite;
import femto.input.Button;
import ZxSpectrum;
import femto.font.TIC80;
import femto.sound.Mixer;
import femto.sound.Procedural;
//import femto.math;
import femto.palette.Pico8;

import EnemyEntity;
import HorizBarEntity;
import VertBarEntity;
import TimeMeterEntity;
import Common;
import Event;
import maps.map;

public class MainDay extends State {
    
    static final int STATE_PLAYING = 0;
    static final int STATE_IN_CHECKPOINT = 1;

    
    // Images
    Winner winnerImage;
    CastleDay castleDayImage;
    Coffee coffee; 
    Bean beanImage; 
    BatEntity bats[]; 

    //HiRes16Color screen; // the screenmode we want to draw with
    TimeMeterEntity timeMeterEntity;
    int currSfxNum;
    float angle; // floats are actually FixedPoint (23.8)
    int counter; // variables are automatically initialized to 0 or null
    boolean isGameOver;
    boolean isWon;
    long programStartTimeMs; // the start time of the program
    float castleDayImageHalfWidth;
    int state = STATE_PLAYING;
    long continueAt; // infinite
    long currentBeanCount;
    long storedBeanCount;  //!!HV
    float castleX;
    float castleY;
    long launchAtScreenTopYOn; // Launch bat at this point
    boolean nextBatGoEast; // Bat direction
   
    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init(){
        //System.gc();
        System.out.println("init(): free=" + java.lang.Runtime.getRuntime().freeMemory());

        Main.screen.loadPalette(Pico8.palette());
        
        Main.screen.cameraX = 0;
        Main.screen.cameraY = 0;
        
        // minus 1 so that the Common.currentFrameStartTimeMs do not start from 0.
        programStartTimeMs = System.currentTimeMillis() - 1; 
        Common.currentFrameStartTimeMs = System.currentTimeMillis() - programStartTimeMs;
        
        //
        timeMeterEntity =  new TimeMeterEntity();
        winnerImage = new Winner();
        castleDayImage = new CastleDay();
        //castleDayImageHalfWidth = castleDayImage.width()/2;
        Common.heroEntity = new HeroEntity(this);
        Common.heroEntity.run();
        coffee = new Coffee();
        coffee.run(); // "run" is one of the animations in the spritesheet
        beanImage = new Bean();
        
        // Bat entity
        bats = new BatEntity[Common.MAX_BATS]; //
        for(int i=0; i < Common.MAX_BATS; i++)
            bats[i] = new BatEntity();
            

        //
        castleX = 110-(castleDayImage.width()/2);
        
        // Create events.
        Common.events = new Event[Common.MAX_EVENTS]; //
        for(int i=0; i < Common.MAX_EVENTS; i++) Common.events[i] = new Event();
        
        // Create enemies
        Common.enemies = new EnemyEntity[Common.MAX_ENEMIES]; //
        int j=0;
        for(int i=0; i < Common.MAX_ENEMIES; i++, j++)
            Common.enemies[j] = new EnemyEntity();
        Common.enemies[0].x = 110;
        Common.enemies[0].followHeroOffsetX = 0;
        Common.enemies[0].followHeroOffsetY = Common.SKELETON_OFFSET_Y;
        Common.enemies[0].run();
        Common.enemies[1].x = 110;
        Common.enemies[1].followHeroOffsetX = -12;
        Common.enemies[1].followHeroOffsetY = Common.SKELETON_OFFSET_Y-20;
        Common.enemies[1].run2();
        Common.enemies[2].x = 110;
        Common.enemies[2].followHeroOffsetX = 12;
        Common.enemies[2].followHeroOffsetY = Common.SKELETON_OFFSET_Y-25;
        Common.enemies[2].run();
        Common.enemies[2].setMirrored(true);

        Common.tilemap = new map();
        
        // Initialize tutoriel bubbles
        if( Common.isTutorialActive )
        {
            int arrCount = 5;
            String textLineArray1[] = new String[arrCount];
            String textLineArray2[] = new String[arrCount];
            //                  "12345678901"
            textLineArray1[0] = "Move to left";
            textLineArray2[0] = "and right";
            textLineArray1[1] = "to collect";
            textLineArray2[1] = "beans.";
            textLineArray1[2] = "Get enough coffee";
            textLineArray2[2] = "to the store to";
            textLineArray1[3] = "survive the night!";
            textLineArray2[3] = "";
            textLineArray1[4] = "Beware of skeletons";
            textLineArray2[4] = "and avoid bushes.";
            Common.events[Main.getNextFreeEvent()].setTutorialBubbleEvent((long)1*1000, Common.bubbleImage, textLineArray1, textLineArray2, arrCount );
        }
        

        System.out.println("2. init(): free=" + java.lang.Runtime.getRuntime().freeMemory());
        
        //!!!HV
        //currentBeanCount = 500;
        //Common.heroEntity.y = 95*16;
    }
    
    // 
    void shutdown(){
        Common.freeResources();
    }
    
    // update is called by femto.Game every frame
    void update(){
        
        // long currTime = System.currentTimeMillis();
        
        // Read the current time. Substract the program start time to make the value smaller, so it fits to 32 bits. 
        Common.currentFrameStartTimeMs = System.currentTimeMillis() - programStartTimeMs;
        //System.out.println("update().currentFrameStartTimeMs=" + Common.currentFrameStartTimeMs);
        
        // Change to a new state when A is pressed
        if( Button.A.justPressed() )
        {
            if(isWon)
            {
                // Go on to the night level.
                Game.changeState( new Main() );
            }
            else if(isGameOver)
            {
                // Restart the game
                Common.totalBeanCount = 0;
                Common.totalCoffeeCount = 0;
                currentBeanCount = 0;
                if( Common.isTutorialActive )
                    Game.changeState( new MainDay() );  // repeat the day level in tutorial mode
                else
                    Game.changeState( new MainLevelMap() );
            }
        }
 
        // Exit to menu
        else  if( Button.C.justPressed() )
        {
            // Restart the game
            Common.totalBeanCount = 0;
            Common.totalCoffeeCount = 0;
            currentBeanCount = 0;
            
            // Goto startup screen
            Common.isTutorialActive = false;
            Game.changeState( new MainStartupScreen() );
       }
            
           
        // *** UPDATE

        if(!isGameOver && ! isWon && state == STATE_PLAYING)
        {
            // Events
            for(int i=0; i < Common.MAX_EVENTS; i++)
            {
                if(Common.events[i].eventTime > 0 && Common.currentFrameStartTimeMs > Common.events[i].eventTime)
                    Common.events[i].execute();
            }

            //
            Common.heroEntity.update(Main.screen);

            for(int i=0; i < Common.MAX_ENEMIES; i++)
                Common.enemies[i].update(Main.screen);

            // Move camera to keep hero vertically in the center
            //System.out.println("height="+Common.tilemap.height()+", tileHeight="+Common.tilemap.tileHeight());
            Main.screen.cameraY = Common.heroEntity.y - 88;
            if(Main.screen.cameraY > Common.tilemap.height() * Common.tilemap.tileHeight() - 176 )
                Main.screen.cameraY = Common.tilemap.height() * Common.tilemap.tileHeight() - 176;
                
            // Set castle position
            float halfTilemapHeight = (Common.tilemap.height() / 2) *16;
            if( Common.heroEntity.y < halfTilemapHeight +  castleDayImage.height() + 176)
            {
                castleY = halfTilemapHeight-castleDayImage.height();
            }
            else
            {
                castleY =(Common.tilemap.height()*16)-castleDayImage.height();
            }
            
            // Should launch a new bat?
            if(Main.screen.cameraY > launchAtScreenTopYOn)
            {
                if( launchAtScreenTopYOn > 0)
                {
                    // find next free bat
                    int i=0;
                    for(; i < Common.MAX_BATS; i++)
                        if( ! bats[i].isActive )
                            break;
                    if( i >= Common.MAX_BATS )
                        i = 0; // No free bats. Just steal the first one(!).
                   
                    long vel = 3;
                    if( nextBatGoEast )
                    {
                        bats[ i ].x = -16;
                        bats[ i ].velX = vel;
                    }
                    else
                    {
                        bats[ i ].x = 220;
                        bats[ i ].velX = -vel;
                    }
                    bats[ i ].y = launchAtScreenTopYOn + 150;
                    bats[ i ].velY = 0;
                    bats[ i ].isHit = false;
                    bats[ i ].playSfx();
                }
                
                // Next bat parameters
                nextBatGoEast = !nextBatGoEast;
                launchAtScreenTopYOn += 500 - (Common.currentDay * 70 );
            }

            
            // update bats
            for(int i=0; i < Common.MAX_BATS; i++)
                bats[i].update(Main.screen);
        }
        
        // *** DRAW
        
        //
        Common.tilemap.drawDayField(Main.screen);

        // Draw hero        
        Common.heroEntity.drawMe( Main.screen );
            
        // Draw enemies
        for(int i=0; i < Common.MAX_ENEMIES; i++)
        {
            Common.enemies[i].drawMe(Main.screen);
        }
            
        // Draw bats        
        for(int i=0; i < Common.MAX_BATS; i++)
            bats[i].drawMe(Main.screen);
    
        // Draw castle
        castleDayImage.draw(Main.screen, castleX, castleY);
        
        // Draw bean count 
        if(state == STATE_IN_CHECKPOINT)
        {
            updateAndDrawBeanCount();
        }
        
        // Draw Common.events
        for(int i=0; i < Common.MAX_EVENTS; i++)
        {
            if(Common.events[i].eventTime > 0 && Common.events[i].isDrawingState)
                Common.events[i].draw(Main.screen);
        }
        
        // Print time meter
        float normalizedDistanceToEnd = 
            (((float)Common.tilemap.height()*16) - (float)(Common.heroEntity.y+Common.heroEntity.height())) / ((float)Common.tilemap.height()*16);
        if(normalizedDistanceToEnd < 0) 
            normalizedDistanceToEnd = 0;
        timeMeterEntity.drawMeter( Main.screen, normalizedDistanceToEnd );
            
        // Draw the winner or loser dialog
        if(isWon)
            DrawWinnerDayDialog();
        else if(isGameOver)
            Main.DrawLoserDialog();

        // Draw status pane background.
        Main.screen.fillRect(0,0, 220, 8, 0 );

        // Print beans number
        beanImage.draw(Main.screen, 110 - 7, 0, false, false, true);
        Main.screen.setTextPosition( 110+1, 0 );
        Main.screen.textColor = 3;
        Main.screen.print( (int)currentBeanCount );
        
        // Print day number
        Main.screen.setTextPosition( 220-(7*6), 0 );
        Main.screen.textColor = 3;
        Main.screen.print("Day: " + (int)(Common.currentDay+1) );
        
        // 
        if(Common.isTutorialActive)
            Main.drawButtonAndLabel( 0, 176-17, 220,"C  Exit tutorial", "C");

        // print fps
        Main.screen.setTextPosition( 0, 0 );
        Main.screen.textColor = 3;
        Main.screen.print("FPS:" + (int)(Main.screen.fps()+0.5));
        
        //!!HV
        //System.out.println("totalBeanCount=" + Common.totalBeanCount + ", currentBeanCount=" + currentBeanCount);
        
        // Update the screen with everything that was drawn
        Main.screen.flush();
    }
    
    
    void updateAndDrawBeanCount()
    {
        
        // Update
        storedBeanCount += 1;
        if(storedBeanCount > currentBeanCount)
        {
            storedBeanCount = currentBeanCount;
            
            //  Beans have been counted
            if(continueAt == 0)
            {
                // *** start to wait
                
                // Not yet started to wait
                System.out.println("updateAndDrawBeanCount(). wait 2 seconds");
                // After beans have been counted wait for a little while before continuing the game.
                continueAt = Common.currentFrameStartTimeMs + 2*1000;
            }
            else if(Common.currentFrameStartTimeMs > continueAt )
            {
                // *** continue running
                
                System.out.println("updateAndDrawBeanCount(). state == STATE_PLAYING");
                // Stop waiting in the check-point and continue playing.
                state = STATE_PLAYING;
                // Move the hero past the castle
                Common.heroEntity.y = castleY + castleDayImage.height() + 2;
                //Common.totalBeanCount += currentBeanCount;
                float offsetChange = Common.SKELETON_OFFSET_Y - Common.enemies[0].followHeroOffsetY; 
                for(int i=0; i < Common.MAX_ENEMIES; i++)
                    Common.enemies[i].followHeroOffsetY += offsetChange;
            }
        }
        
        // 
        float px = castleX + 23 - Main.screen.cameraX;
        float py = castleY + 20 - Main.screen.cameraY;

        Main.screen.textColor = 14;
        Main.screen.setTextPosition( px+1, py );
        Main.screen.print((int)storedBeanCount);
        Main.screen.setTextPosition( px-1, py );
        Main.screen.print((int)storedBeanCount);
        Main.screen.setTextPosition( px, py+1 );
        Main.screen.print((int)storedBeanCount);
        Main.screen.setTextPosition( px, py-1 );
        Main.screen.print((int)storedBeanCount);

        Main.screen.setTextPosition( px, py );
        Main.screen.textColor = 3;
        Main.screen.print((int)storedBeanCount);
    }

    //
    public void DrawWinnerDayDialog()
    {
        int winX = 0;
        int winY = 88-30;
        int winW = 220;
        int winH = 2*30 + 3;
        int marginV = 2;
        
        Main.DrawPanel(winX, winY, winW, winH);
        
        int startCoffeeX = (int)Main.screen.cameraX + 80;
        int startCoffeeY = (int)Main.screen.cameraY + winY + 10;
        
        Common.totalCoffeeCount = Common.totalBeanCount / 40;
        
        if(Common.totalCoffeeCount > 0)
            coffee.draw(Main.screen, startCoffeeX, startCoffeeY);
        if(Common.totalCoffeeCount > 1)
            coffee.draw(Main.screen, startCoffeeX + 20, startCoffeeY);
        if(Common.totalCoffeeCount > 2)
            coffee.draw(Main.screen, startCoffeeX + 40, startCoffeeY);
        if(Common.totalCoffeeCount > 3)
            coffee.draw(Main.screen, startCoffeeX + 60, startCoffeeY);
        if(Common.totalCoffeeCount > 4)
            coffee.draw(Main.screen, startCoffeeX + 80, startCoffeeY);
            
        Main.drawTextCellCentered( winX, winY + 30, winW, "That is your supply for the night!" );
        Main.drawButtonAndLabel( winX, winY + 45, winW, "A  Continue", "A");
    }

}


