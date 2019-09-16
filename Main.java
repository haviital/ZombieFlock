// Code: by Hannu Viitala, MIT license. 

// Graphics:
// @hanski, CCBY 3.0
// @vampirics, CCBY 3.0

// Sounds from Freesound.org:
// Breviceps, CC0 1.0
// CrocyTC, CC0 1.0
// ArriGD, CC0 1.0
// Missozzy, CCBY 3.0
// Missozzy, CCBY 3.0
// Thanra, CC0 1.0

import femto.mode.HiRes16Color;
import femto.Game;
import femto.State;
import femto.Sprite;
import femto.Image;
import femto.input.Button;
import ZxSpectrum;
import femto.font.TIC80;
import femto.sound.Mixer;
import femto.sound.Procedural;
//import femto.math;
import femto.palette.Pico8;

import MainDay;
import MainLevelMap;
import ZombieEntity;
import HorizBarEntity;
import VertBarEntity;
import TimeMeterEntity;
import Common;
import Event;

class LevelPoints extends femto.Cookie {
    LevelPoints(){
        super();
        begin("ZOMFLOCK"); // name of the cookie, up to 8 chars
    }
    int versionMajor;
    int versionMinor;
    int levelPoints0; 
    int levelPoints1; 
    int levelPoints2; 
    int levelPoints3; 
    int levelPoints4; 
    int levelPoints5; 
}

public class Main extends State {
    
    // Images
    Coffee coffee; // an animated sprite imported from Aseprite
    Pattern background; // static image
    Bubble bubbleImage;
    Castle castleImage;
    Zombie zombieWithCoffee;
    Winner winnerImage;

    // Sounds
    arrigd_zombie_roar_3 sfx1;
    breviceps_zombie_gargles sfx2;
    crocytc_zombie3 sfx3;
    missozzy_zombie_02 sfx4;
    missozzy_zombie_04 sfx5;
    thanra_zombie_roar sfx6;
    
    //HiRes16Color screen; // the screenmode we want to draw with
    HorizBarEntity barH;
    VertBarEntity barV;
    TimeMeterEntity timeMeterEntity;
    int currSfxNum;
    float angle; // floats are actually FixedPoint (23.8)
    int counter; // variables are automatically initialized to 0 or null
    boolean[] isCoffeeTaken; 
    boolean isGameOver;
    boolean isWon;
    long programStartTimeMs; // the start time of the program
    
    
    // *** static variables and functions
    
    static final var levelPoints = new LevelPoints();
    
    public static HiRes16Color screen = new HiRes16Color(Pico8.palette(), TIC80.font());
    
    
    // start the game using Main as the initial state
    // and TIC80 as the menu's font
    public static void main(String[] args){
        
        // Create the level map.
        Common.levelPointsArray = new int[Common.LEVEL_MAP_COUNT];
        Common.levelPointsArray[0] = levelPoints.levelPoints0;
        Common.levelPointsArray[1] = levelPoints.levelPoints1;
        Common.levelPointsArray[2] = levelPoints.levelPoints2;
        Common.levelPointsArray[3] = levelPoints.levelPoints3;
        Common.levelPointsArray[4] = levelPoints.levelPoints4;
        Common.levelPointsArray[5] = levelPoints.levelPoints5;

        Common.currentDay = 1;
        Game.run( TIC80.font(), new MainLevelMap() );
        //Game.run( TIC80.font(), new MainDay() );
        //Game.run( TIC80.font(), new Main() );
    }
    
    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init(){
        //System.gc();
        System.out.println("init(): free=" + java.lang.Runtime.getRuntime().freeMemory());
        programStartTimeMs = System.currentTimeMillis() - 1; // minus 1 so that the Common.currentFrameStartTimeMs do not start from 0.
        
        //!!HV make the level shorter
        programStartTimeMs -= 25*1000;
        
        Common.currentFrameStartTimeMs = System.currentTimeMillis() - programStartTimeMs;
        
        screen.cameraX = 0;
        screen.cameraY = 0;
        
        //
        background = new Pattern();
        bubbleImage =  new Bubble();
        castleImage = new Castle();
        barH = new HorizBarEntity(0, 88-2);
        barV = new VertBarEntity(110-2, 0);
        timeMeterEntity =  new TimeMeterEntity();
        winnerImage = new Winner();
        
        sfx1 = new arrigd_zombie_roar_3(0);
        sfx2 = new breviceps_zombie_gargles(0);
        sfx3 = new crocytc_zombie3(0);
        sfx4 = new missozzy_zombie_02(0);
        sfx5 = new missozzy_zombie_04(0);
        sfx6 = new thanra_zombie_roar(0);
        currSfxNum = 1;

        // Initialize the Mixer at 8khz
        Mixer.init(8000);
        
        // Create events.
        Common.events = new Event[Common.MAX_EVENTS]; //
        for(int i=0; i < Common.MAX_EVENTS; i++) Common.events[i] = new Event();
        
        zombieWithCoffee = new Zombie();
        isCoffeeTaken = new boolean[5];
        
        Common.zombies = new ZombieEntity[Common.MAX_ZOMBIES]; //
        int j=0;
        for(int i=0; i < Common.ZOMBIES_PER_NEST; i++, j++)
        {
            Common.zombies[j] = new ZombieEntity( new Zombie() );
            Common.zombies[j].setX(1*i*5);
            Common.zombies[j].setY(-1*i*5);
            Common.zombies[j].sprite.run();
        }
        for(int i=0; i < Common.ZOMBIES_PER_NEST; i++, j++)
        {
            Common.zombies[j] = new ZombieEntity( new Zombie() );
            Common.zombies[j].setX(-20+220 + (1*i*5));
            Common.zombies[j].setY(-40 +(1*i*5));
            Common.zombies[j].sprite.run();
        }
        for(int i=0; i < Common.ZOMBIES_PER_NEST; i++, j++)
        {
            Common.zombies[j] = new ZombieEntity( new Zombie() );
            Common.zombies[j].setX(-20+220 + (1*i*5));
            Common.zombies[j].setY(40+176+(-1*i*5));
            Common.zombies[j].sprite.run();
        }
        for(int i=0; i < Common.ZOMBIES_PER_NEST; i++, j++)
        {
            Common.zombies[j] = new ZombieEntity( new Zombie() );
            Common.zombies[j].setX(1*i*5);
            Common.zombies[j].setY(176+(1*i*5));
            Common.zombies[j].sprite.run();
        }
       
        coffee = new Coffee();
        coffee.run(); // "run" is one of the animations in the spritesheet
        
        int arrCount = 6;
        String textLineArray1[] = new String[arrCount];
        String textLineArray2[] = new String[arrCount];
        textLineArray1[0] = "Zombies are";
        textLineArray2[0] = "coming!";
        textLineArray1[1] = "They want";
        textLineArray2[1] = "THE COFFEE!";
        textLineArray1[2] = "We need to";
        textLineArray2[2] = "push'em away";
        textLineArray1[3] = "Zzzz.... oh!";
        textLineArray2[3] = "I am sleepy.";
        textLineArray1[4] = "HAHAHAHAAA!";
        textLineArray2[4] = "MORE COFFEE!";
        textLineArray1[5] = "Hey! How are";
        textLineArray2[5] = "you today?";
        //Common.events[getNextFreeEvent()].setTutorialBubbleEvent((long)1*1000, bubbleImage, textLineArray1, textLineArray2, arrCount );
        
        // Set Common.events for random sounds.
        Common.events[getNextFreeEvent()].setSfxEvent((long)3*1000, sfx2, 15000, 40000 );
        
        System.out.println("2. init(): free=" + java.lang.Runtime.getRuntime().freeMemory());
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
            if(isWon || isGameOver)
            {
                // Go on to the map.
                //Common.currentDay += 1;
                Common.currentDay = 0;
                Common.totalBeanCount = 0;
                Common.totalCoffeeCount = 0;
                Game.changeState( new MainLevelMap() );
            }
        }
            
        if( Button.B.justPressed() )
        {
             if(currSfxNum==1) sfx1.play();
             else if(currSfxNum==2) sfx2.play();
             else if(currSfxNum==3) sfx3.play();
             else if(currSfxNum==4) sfx4.play();
             else if(currSfxNum==5) sfx5.play();
             else if(currSfxNum==6) sfx6.play();
            
             currSfxNum += 1;
             if( currSfxNum>6 ) currSfxNum = 1;
        }

        // *** Update 
        
        if(!isGameOver)
        {
            // Events
            for(int i=0; i < Common.MAX_EVENTS; i++)
            {
                if(!Common.events[i].isFree() && Common.currentFrameStartTimeMs > Common.events[i].eventTime)
                    Common.events[i].execute();
            }

             for(int i=0; i < Common.MAX_ZOMBIES; i++)
            {
                Common.zombies[i].update();
            }
    
            //
            barH.update();
            barV.update();
            
            //
            checkZombiesCollisionToCastle();
            
        }
        
        // *** Draw
        
        // Fill the screen using Pattern.png
        for( int y=0; y<176; y += background.height() ){
            for( int x=0; x<220; x += background.width() ){
                background.draw(screen, x, y);
            }
        }

        // Draw  zombies      
        for(int i=0; i < Common.MAX_ZOMBIES; i++)
        {
            Common.zombies[i].draw(screen);
        }
        
        // Draw bars
        barV.draw(screen);
        barH.draw(screen);
        
        // Draw the castle.
        if(!isGameOver)
            castleImage.draw(screen, Common.CASTLE_X, Common.CASTLE_Y);
       
        // Draw coffee or zombie on tower.
        drawCoffees();
        
        // Draw Common.events
        for(int i=0; i < Common.MAX_EVENTS; i++)
        {
            if(!Common.events[i].isFree() && Common.events[i].isDrawingState)
                Common.events[i].draw(screen);
        }
        
        // Set holes
        float normalizedTimeLeft = ((float)Common.LEVEL_TIMEOUT - Common.currentFrameStartTimeMs) / (float)Common.LEVEL_TIMEOUT;
        if(normalizedTimeLeft <= 0) 
        {
            // Night is over!
            
            if(!isGameOver && !isWon)
            {
                // Won the game!
                isWon = true;
                isGameOver = true;
                
                // If this is a new record, save it to the EEPROM.
                if( Common.totalCoffeeCount > Common.levelPointsArray[ Common.currentDay ] )
                {
                    Common.levelPointsArray[ Common.currentDay ] = Common.totalCoffeeCount;
                    saveLevelPointsToEEPROM();
                }
            }
            normalizedTimeLeft = 0;
        }
        if( normalizedTimeLeft < 0.5)
        {
            barV.isHiddenPart[1] = true;
            barV.isHiddenPart[9] = true;
        }
        if( normalizedTimeLeft < 0.25)
        {
            barH.isHiddenPart[1] = true;
            barH.isHiddenPart[11] = true;
        }

        // Print time meter
        timeMeterEntity.drawMeter( screen, 
            normalizedTimeLeft );
            
        // Draw the winner or loser dialog
        if(isWon)
            DrawWinnerDialog(winnerImage);
        else if(isGameOver)
            DrawLoserDialog();

        // Print day number
        Main.screen.setTextPosition( 220-(7*6), 0 );
        Main.screen.textColor = 3;
        Main.screen.print("Day: " + (int)Common.currentDay );
        
        // print fps
        screen.setTextPosition( 0, 0 );
        screen.textColor = 3;
        screen.print((int)(screen.fps()+0.5));
        
        // Update the screen with everything that was drawn
        screen.flush();
    }
    

    void checkZombiesCollisionToCastle()
    {
        //sprite.draw(screen); // Animation is updated automatically
        //System.out.println("checkZombiesCollisionToCastle(). START");
        
        // Castle rect
        float castX = Common.CASTLE_X+3;
        float castY = Common.CASTLE_Y+3;
        float castW = castleImage.width()-6;
        float castH = castleImage.height()-6;
        
        for(int i=0; i < Common.MAX_ZOMBIES; i++)
        {
            ZombieEntity zomb =  Common.zombies[i];
            float zombX = zomb.getX();
            float zombY = zomb.getY();
            float zombW = zomb.sprite.width();
            float zombH = zomb.sprite.height();

            //System.out.println("checkZombiesCollisionToCastle(). i=" + i);
            
            boolean isIntersection = 
                (castX < zombX + zombW) && 
                (castY < zombY + zombH) &&
                (castX + castW > zombX) && 
                (castY + castH > zombY);
                
            // If zombie touches the castle, it will drink one coffee!
            if(isIntersection)
            {
                // Mark the coffee as taken
                Common.totalCoffeeCount -= 1;
                if(Common.totalCoffeeCount>=0)
                    isGameOver = true;
                for(int j=0; j < 5; j++ )
                    if(!isCoffeeTaken[j])
                    {
                        //System.out.println("checkZombiesCollisionToCastle(). i=" + i + ", j=" + j);
                        isCoffeeTaken[j] = true;
                        //if(j>=Common.totalCoffeeCount) 
                        //    isGameOver = true;
                        break;
                    }
                
                // Put the zombie away    
                zomb.setX(-10);
            }
         }
    }
    
    // Draw coffee or zombie on tower.
    void drawCoffees()
    {
        zombieWithCoffee.setMirrored(false);
        zombieWithCoffee.setFlipped(false);
        
        if(Common.totalCoffeeCount>0)
            if(isCoffeeTaken[0])
                zombieWithCoffee.draw(screen, 92,         70);
            else 
                coffee.draw(screen, 92,         70);
        if(Common.totalCoffeeCount>1)
            if(isCoffeeTaken[1])
                zombieWithCoffee.draw(screen, 92+12,      70+10);
            else
                coffee.draw(screen, 92+12,      70+10);
        if(Common.totalCoffeeCount>2)
            if(isCoffeeTaken[2])
                zombieWithCoffee.draw(screen, 92+12+10,   70);
            else
                coffee.draw(screen, 92+12+10,   70);
        if(Common.totalCoffeeCount>3)
            if(isCoffeeTaken[3])
                zombieWithCoffee.draw(screen, 92,         92);
            else
                coffee.draw(screen, 92,         92);
        if(Common.totalCoffeeCount>4)
            if(isCoffeeTaken[4])
                zombieWithCoffee.draw(screen, 92+12+10,   92);
            else
                coffee.draw(screen, 92+12+10,   92);
    }

    void saveLevelPointsToEEPROM()
    {
        levelPoints.levelPoints0 = Common.levelPointsArray[0];
        levelPoints.levelPoints1 = Common.levelPointsArray[1];
        levelPoints.levelPoints2 = Common.levelPointsArray[2];
        levelPoints.levelPoints3 = Common.levelPointsArray[3];
        levelPoints.levelPoints4 = Common.levelPointsArray[4];
        levelPoints.levelPoints5 = Common.levelPointsArray[5];
        //System.out.println("Main.levelPoints.levelPoints0=" + Main.levelPoints.levelPoints0);
        levelPoints.saveCookie();
    }
    
    public static void DrawWinnerDialog(Image winnerImage)
    {
        int winX = 0;
        int winY = 88-30;
        int winW = 220;
        int winH = 2*30;
        int marginV = 2;
        Main.screen.fillRect( winX, winY, winW, winH, 15 );
        Main.screen.fillRect( winX, winY+marginV, winW, winH-(2*marginV), 13 );
        
        winnerImage.draw(Main.screen, 110 - 49,  winY + marginV + 2);
        winnerImage.draw(Main.screen, 110 - 13,  winY + marginV + 2);
        winnerImage.draw(Main.screen, 110 + 23,  winY + marginV + 2);

        Main.screen.setTextPosition( winX, winY + 30);
        Main.screen.textColor = 3;
        Main.screen.print("    You made it throught the night!");
        Main.screen.setTextPosition( winX, winY  + 45);
        Main.screen.print("     Continue to the next day (A)?");

        //Main.screen.setTextPosition( 80, 88  + 15);
        //Main.screen.textColor = 3;
        //Main.screen.print("YOU WON!");
    }


        //
    public static void DrawLoserDialog()
    {
        int winX = 0;
        int winY = 88-30;
        int winW = 220;
        int winH = 2*30;
        int marginV = 2;
        screen.fillRect( winX, winY, winW, winH, 15 );
        screen.fillRect( winX,  winY+marginV, winW, winH-(marginV*2), 13 );
        
        screen.setTextPosition( winX,   winY + 10);
        screen.textColor = 3;
        screen.print(" You lost! Undeads will rule the world");
        screen.setTextPosition( winX,   winY + 20);
        screen.print("     with the power of coffee!");
        screen.setTextPosition( winX, winY + 40);
        screen.print("           Restart (A)?");
    }

    public static int getNextFreeEvent()
    {
        int foundIndex = -1;
        int freeCount;
        for(int i=0; i < Common.MAX_EVENTS; i++)
        {
            if(Common.events[i].isFree()) 
            {
                freeCount += 1;
                if(foundIndex==-1)
                    foundIndex = i;
            }
        }
        System.out.println("Free events: " + freeCount);
        return foundIndex;
    }
    
}


