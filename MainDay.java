
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
    Bubble bubbleImage;
    Winner winnerImage;
    CastleDay castleDayImage;
    Coffee coffee; 
    Bean beanImage; 

    // Sounds
    arrigd_zombie_roar_3 sfx1;
    breviceps_zombie_gargles sfx2;
    crocytc_zombie3 sfx3;
    missozzy_zombie_02 sfx4;
    missozzy_zombie_04 sfx5;
    thanra_zombie_roar sfx6;
    
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
    long dialogBeanCount;  //!!HV
    float castleX;
    float castleY;
   
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
        bubbleImage =  new Bubble();
        timeMeterEntity =  new TimeMeterEntity();
        winnerImage = new Winner();
        castleDayImage = new CastleDay();
        //castleDayImageHalfWidth = castleDayImage.width()/2;
        Common.heroEntity = new HeroEntity(this);
        Common.heroEntity.run();
        coffee = new Coffee();
        coffee.run(); // "run" is one of the animations in the spritesheet
        beanImage = new Bean();
       
        //
        castleX = 110-(castleDayImage.width()/2);
        
        // Create events.
        Common.events = new Event[Common.MAX_EVENTS]; //
        for(int i=0; i < Common.MAX_EVENTS; i++) Common.events[i] = new Event();
        
        // Create enemies
        Common.enemies = new EnemyEntity[Common.MAX_ENEMIES]; //
        int j=0;
        for(int i=0; i < Common.MAX_ENEMIES; i++, j++)
        {
            Common.enemies[j] = new EnemyEntity();
        }
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
        
        // sfx1 = new arrigd_zombie_roar_3(0);
        // sfx2 = new breviceps_zombie_gargles(0);
        // sfx3 = new crocytc_zombie3(0);
        
        
        // sfx4 = new missozzy_zombie_02(0);
        // sfx5 = new missozzy_zombie_04(0);
        // sfx6 = new thanra_zombie_roar(0);
        // currSfxNum = 1;

        // Initialize the Mixer at 8khz
        //Mixer.init(8000);
        
        //Common.events = new Event[Common.MAX_EVENTS]; //
        //for(int i=0; i < Common.MAX_EVENTS; i++)
        //{
        //    Common.events[i] = new Event();
        //}
        
        // Set Common.events for random sounds.
        //Common.events[4].setSfxEvent((long)3*1000, sfx2, 15000, 40000 );
        
        System.out.println("2. init(): free=" + java.lang.Runtime.getRuntime().freeMemory());
        
        //!!HV
        //Common.totalBeanCount = 500;
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
                Game.changeState( new MainLevelMap() );
            }
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
            {
                Common.enemies[i].update(Main.screen);
            }
    
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
                castleY = (Common.tilemap.height()*16)-castleDayImage.height();
            }

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
        float normalizedTimeLeft = ((float)Common.LEVEL_TIMEOUT - Common.currentFrameStartTimeMs) / (float)Common.LEVEL_TIMEOUT;
        if(normalizedTimeLeft <= 0) 
        {
            if(!isGameOver)
            {
                //isWon = true;
                //isGameOver = true;
            }
            normalizedTimeLeft = 0;
        }
        timeMeterEntity.drawMeter( Main.screen, normalizedTimeLeft );
            
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
        Main.screen.print("Day: " + (int)Common.currentDay );
        
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
        dialogBeanCount += 1;
        if(dialogBeanCount > currentBeanCount)
        {
            dialogBeanCount = currentBeanCount;
            
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
        Main.screen.setTextPosition( castleX + 10 - Main.screen.cameraX, castleY + 30 - Main.screen.cameraY );
        Main.screen.textColor = 3;
        Main.screen.print((int)dialogBeanCount);
    }

    //
    public void DrawWinnerDayDialog()
    {
        int winX = 0;
        int winY = 88-30;
        int winW = 220;
        int winH = 2*30;
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
            
        Main.screen.setTextPosition( winX+15, winY + 30);
        Main.screen.textColor = 3;
        Main.screen.print("That is your supply for the night!");
        Main.screen.setTextPosition( winX+5+60, winY  + 45);
        Main.screen.print("Continue (A)?");
    }

}


