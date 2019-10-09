//
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
import MainLevelMap;b
import ZombieEntity;
import HorizBarEntity;
import VertBarEntity;
import TimeMeterEntity;
import Common;
import Event;

class EepromCookie extends femto.Cookie {
    
    static final long FLAG_TUTORIAL_PASSED = 1;
    
    EepromCookie(){
        super();
        flags = 0;
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
    long flags;
}

class Vector2d
{
    float x;
    float y;
}


public class Main extends State {
    
    // Images
    Coffee coffee; // an animated sprite imported from Aseprite
    Pattern background; // static image
    Castle castleImage;
    Zombie zombieWithCoffee;
    Winner winnerImage;
    BombEntity bombs[];

    // Sounds
    breviceps_zombie_gargles sfx2;
    breviceps__step_into_water_puddle_wade zombiTakesCoffeeSfx;

    HorizBarEntity barH;
    VertBarEntity barV;
    TimeMeterEntity timeMeterEntity;
    //int currSfxNum;
    float angle; // floats are actually FixedPoint (23.8)
    int counter; // variables are automatically initialized to 0 or null
    boolean[] isCoffeeTaken; 
    boolean isGameOver;
    boolean isWon;
    long programStartTimeMs; // the start time of the program
    long launchBombAt; // Launch bomb at this time.
    Vector2d bombCoords[];
    int bombCoordsMax = 4;
    int bombCoordsNum = 0;
    
    // *** static variables and functions
    
    static final var eepromCookie = new EepromCookie();
    
    public static HiRes16Color screen = new HiRes16Color(Pico8.palette(), TIC80.font());
    
    
    // start the game using Main as the initial state
    // and TIC80 as the menu's font
    public static void main(String[] args){
        
         // Initialize the Mixer at 8khz
        Mixer.init(8000);
        
       // Create the level map.
        Common.levelPointsArray = new int[Common.LEVEL_MAP_COUNT];
        Common.levelPointsArray[0] = eepromCookie.levelPoints0;
        Common.levelPointsArray[1] = eepromCookie.levelPoints1;
        Common.levelPointsArray[2] = eepromCookie.levelPoints2;
        Common.levelPointsArray[3] = eepromCookie.levelPoints3;
        Common.levelPointsArray[4] = eepromCookie.levelPoints4;
        Common.levelPointsArray[5] = eepromCookie.levelPoints5;
        
        Common.panelImage1 = new Panel1();
        Common.panelImage2 = new Panel2();
        Common.panelImage4 = new Panel4();
        Common.panelImage6 = new Panel6();
        Common.panelImage7 = new Panel7();
        Common.panelHighLightImage = new PanelHighLight();

        Common.bubbleImage =  new Bubble();
        Common.bubbleVertImage =  new BubbleVert();
        Common.bubbleHorizImage =  new BubbleHoriz();
        Common.bubbleCornerImage =  new BubbleCorner();
        Common.bubbleTipImage =  new BubbleTip();
        Common.mattiImage = new Matti(); 
        Common.uiButtonImage = new UiButton();
        
        Common.currentDay = 0;
 
          //!!!HV
        eepromCookie.flags |= eepromCookie.FLAG_TUTORIAL_PASSED;

        Game.run( TIC80.font(), new MainStartupScreen() );
        //Game.run( TIC80.font(), new MainDay() );
        //Game.run( TIC80.font(), new Main() );
    }
    
    // Avoid allocation in a State's constructor.
    // Allocate on init instead.
    void init(){
        //System.gc();
        System.out.println("init(): free=" + java.lang.Runtime.getRuntime().freeMemory());
        programStartTimeMs = System.currentTimeMillis() - 1; // minus 1 so that the Common.currentFrameStartTimeMs do not start from 0.
        
        //!!!HV make the level shorter
        //programStartTimeMs -= 20*1000;
        
        Common.currentFrameStartTimeMs = System.currentTimeMillis() - programStartTimeMs;
        
        Main.screen.loadPalette(Pico8.palette());
        
        screen.cameraX = 0;
        screen.cameraY = 0;
        
        //
        background = new Pattern();
        castleImage = new Castle();
        barH = new HorizBarEntity(0, 88-2, this );
        barV = new VertBarEntity( 110-2, 0, this );
        timeMeterEntity =  new TimeMeterEntity();
        winnerImage = new Winner();
        
        sfx2 = new breviceps_zombie_gargles(0);
        zombiTakesCoffeeSfx  = new breviceps__step_into_water_puddle_wade(1);

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
        
        // Bombs
        bombs = new BombEntity[Common.MAX_BOMBS]; //
        for(int i=0; i < Common.MAX_BOMBS; i++)
            bombs[i] = new BombEntity();
            
        // Bomb coords
        bombCoords = new Vector2d[bombCoordsMax]; //
        for(int i=0; i < bombCoordsMax; i++)
            bombCoords[i] = new Vector2d();
        bombCoords[ 0 ].x = 20;
        bombCoords[ 0 ].y = 176-50-bombs[ 0 ].height();
        bombCoords[ 1 ].x = 220-20-bombs[ 0 ].width();
        bombCoords[ 1 ].y = 176-50-bombs[ 0 ].height();
        bombCoords[ 2 ].x = 220-20-bombs[ 0 ].width();
        bombCoords[ 2 ].y = 50;
        bombCoords[ 3 ].x = 20;
        bombCoords[ 3 ].y = 50;
        // launch next bomb
        launchBombAt = Common.currentFrameStartTimeMs + 20000 - (Common.currentDay*3000);
        
        // Initialize tutorial bubbles
        if( Common.isTutorialActive )
        {
            int arrCount = 3;
            String textLineArray1[] = new String[arrCount];
            String textLineArray2[] = new String[arrCount];
            //                  "12345678901"
            textLineArray1[0] = "Zombies are coming!";
            textLineArray2[0] = "They want the COFFEE!";
            textLineArray1[1] = "Move the power";
            textLineArray2[1] = "shields and keep them";
            textLineArray1[2] = "away until the";
            textLineArray2[2] = "morning comes.";
            Common.events[Main.getNextFreeEvent()].setTutorialBubbleEvent((long)1*1000, Common.bubbleImage, textLineArray1, textLineArray2, arrCount );
        }
        
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
                Common.currentDay = 0;
                Common.totalBeanCount = 0;
                Common.totalCoffeeCount = 0;
                if( !isWon && Common.isTutorialActive )
                {
                    // Restart tutorial.
                    Game.changeState( new MainDay() );  // repeat the day level in tutorial mode
                }
                else
                {
                    // Go on to the map.
                    Game.changeState( new MainLevelMap() );
                }
            }
        }
            
        else if( Button.B.justPressed() )
        {
            //  if(currSfxNum==1) sfx1.play();
            //  else if(currSfxNum==2) sfx2.play();
            //  else if(currSfxNum==3) sfx3.play();
            //  else if(currSfxNum==4) sfx4.play();
            //  else if(currSfxNum==5) sfx5.play();
            //  else if(currSfxNum==6) sfx6.play();
            
            //  currSfxNum += 1;
            //  if( currSfxNum>6 ) currSfxNum = 1;
        }

        // Exit to menu
        else  if( Button.C.justPressed() )
        {
            // Restart the game
            Common.totalBeanCount = 0;
            Common.totalCoffeeCount = 0;

            // Goto startup screen
            Common.isTutorialActive = false;
            Game.changeState( new MainStartupScreen() );
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

            // Should launch a new bomb?
            if( launchBombAt < Common.currentFrameStartTimeMs )
            {
                //if( launchAtScreenTopYOn > 0)
                {
                    // find next free bat
                    int i=0;
                    for(; i < Common.MAX_BOMBS; i++)
                        if( bombs[ i ].state == BombEntity.STATE_NOT_VISIBLE )
                            break;
                    if( i >= Common.MAX_BOMBS )
                        i = 0; // No free bombs. Just steal the first one (should not happen!).
                   
                   // Launch bomb
                    bombs[ i ].Launch( (float)bombCoords[ bombCoordsNum ].x, (float)bombCoords[ bombCoordsNum ].y );
                    bombCoordsNum += 1;
                    
                    // launch next bomb
                    launchBombAt = Common.currentFrameStartTimeMs + 20000 - (Common.currentDay*3000);
                }
                
            }
            
            // Update bombs.
            for(int i=0; i < Common.MAX_BOMBS; i++)
                bombs[i].update();
            
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
        
        // Draw bombs.
        for(int i=0; i < Common.MAX_BOMBS; i++)
            bombs[i].drawMe(screen);
            
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
                
                // In tutorial mode, set tutorial passed!
                if( Common.isTutorialActive )
                {
                    eepromCookie.flags |= eepromCookie.FLAG_TUTORIAL_PASSED;
                    saveLevelPointsToEEPROM();
                }
                
                // If this is a new record, save it to the EEPROM.
                if( !Common.isTutorialActive && Common.totalCoffeeCount > Common.levelPointsArray[ Common.currentDay ] )
                {
                    Common.levelPointsArray[ Common.currentDay ] = Common.totalCoffeeCount;
                    saveLevelPointsToEEPROM();
                }
                
            }
            normalizedTimeLeft = 0;
        }
        if( normalizedTimeLeft < 0.5 + (Common.currentDay*0.1) )
        {
            barV.isHiddenPart[1] = true;
            barV.isHiddenPart[9] = true;
            if( Common.currentDay > 3)
                barV.isHiddenPart[5] = true;
        }
        if( normalizedTimeLeft < 0.25 + (Common.currentDay*0.1))
        {
            barH.isHiddenPart[1] = true;
            barH.isHiddenPart[11] = true;
            if( Common.currentDay > 3)
                barV.isHiddenPart[5] = true;
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
        Main.screen.print("Day: " + (int)(Common.currentDay + 1) );
        
        // 
        if(Common.isTutorialActive)
            Main.drawButtonAndLabel( 0, 176-17, 220,"C  Exit tutorial", "C");

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
                if(Common.totalCoffeeCount<=0)
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
                
                // Play sound.
                zombiTakesCoffeeSfx.play();
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
        eepromCookie.levelPoints0 = Common.levelPointsArray[0];
        eepromCookie.levelPoints1 = Common.levelPointsArray[1];
        eepromCookie.levelPoints2 = Common.levelPointsArray[2];
        eepromCookie.levelPoints3 = Common.levelPointsArray[3];
        eepromCookie.levelPoints4 = Common.levelPointsArray[4];
        eepromCookie.levelPoints5 = Common.levelPointsArray[5];
        //System.out.println("Main.levelPoints.levelPoints0=" + Main.levelPoints.levelPoints0);
        eepromCookie.saveCookie();
    }
    
    public static void DrawWinnerDialog(Image winnerImage)
    {
        int winX = 0;
        int winY = 88-30;
        int winW = 220;
        int winH = 2*30;
        int marginV = 2;
        
        DrawPanel(winX, winY, winW, winH);
        
        winnerImage.draw(Main.screen, 110 - 49,  winY + marginV + 2);
        winnerImage.draw(Main.screen, 110 - 13,  winY + marginV + 2);
        winnerImage.draw(Main.screen, 110 + 23,  winY + marginV + 2);

        Main.drawTextCellCentered( winX, winY + 30, winW, "You made it throught the night!" );
        Main.drawButtonAndLabel( winX, winY + 43, winW, "A  Continue", "A");

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
        DrawPanel(winX, winY, winW, winH);
        
        Main.drawTextCellCentered( winX, winY + 10, winW, "You lost! Undeads will rule" );
        Main.drawTextCellCentered( winX, winY + 20, winW, "the world with the power of coffee!" );
        Main.drawButtonAndLabel( winX, winY + 40, winW, "A  Restart", "A");
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
    

    public static void DrawPanel(int x, int y, int w, int h)
    {
        // Fill the center area.
        Main.screen.fillRect( x+16, y+16, w-32, h-32, 12 );

        // Draw edges
        int count = (w - 32) / 16;
        for( int i=0; i<count; i+=1 )
        {
            // Top and bottom edge
            Common.panelImage2.draw(Main.screen, x + 16 + (i*16),  y, false, false, true);
            Common.panelImage7.draw(Main.screen, x + 16 + (i*16),  y + h - 16, false, false, true );
        }
        if(count>0)
        {
            Common.panelImage2.draw(Main.screen, x + w - 32,  y, false, false, true);  // last piece
            Common.panelImage7.draw(Main.screen, x + w - 32,  y + h - 16, false, false, true ); // last piece
        }
        
        count = (h - 16 - 16) / 16;
        for( int j=0; j<count; j+=1 )
        {
            // Left and right edge
            Common.panelImage4.draw(Main.screen, x,           y + 16 + (j*16), false, false, true);
            Common.panelImage4.draw(Main.screen, x + w - 16,  y + 16 + (j*16), true, false, true);
        }
        if(count>0)
        {
            Common.panelImage4.draw(Main.screen, x,           y + h - 32, false, false, true);  // last piece
            Common.panelImage4.draw(Main.screen, x + w - 16,  y + h - 32, true, false, true);  // last piece
        }
        
        // Draw corners
        Common.panelImage1.draw(Main.screen, x,  y, false, false, true);
        Common.panelImage1.draw(Main.screen, x + w - 16,  y, true, false, true);
        Common.panelImage6.draw(Main.screen, x,  y + h - 16, false, false, true);
        Common.panelImage6.draw(Main.screen, x + w - 16,  y + h - 16, true, false, true);
        
        // Draw the hightlight
        Common.panelHighLightImage.draw( Main.screen, x+8, y+6, false, false, true );
    }
    
    public static void drawBubble(int x, int y, int w, int h, int tipRelPosX)
    {
        // Fill the center area.
        Main.screen.fillRect( x+4, y+4, w-8, h-8, 3 );

        // Draw edges
        int count = (w-4-4) / 16;
        for( int i=0; i<count; i+=1 )
        {
            // Top and bottom edge
            Common.bubbleHorizImage.draw(Main.screen, x + 4 + (i*16),  y, false, false, true);
            Common.bubbleHorizImage.draw(Main.screen, x + 4 + (i*16),  y + h - 4, false, true, true);
        }
        // Draw last piece
        if(count>0)
        {
            Common.bubbleHorizImage.draw(Main.screen, x + w - 20,  y, false, false, true); 
            Common.bubbleHorizImage.draw(Main.screen, x + w - 20,  y + h - 4, false, true, true );
        }
        
        // Draw tip
        boolean tipMirrored = tipRelPosX > (w/2);
        Common.bubbleTipImage.draw(Main.screen, x + tipRelPosX,  y + h - 4, tipMirrored, false, true);
        
        count = (h - 4 - 4) / 16;
        for( int j=0; j<count; j+=1 )
        {
            // Left and right edge
            Common.bubbleVertImage.draw(Main.screen, x,          y + 4 + (j*16), false, false, true);
            Common.bubbleVertImage.draw(Main.screen, x + w - 4,  y + 4 + (j*16), true, false, true);
        }
        // Draw last piece
        if(count>0)
        {
            Common.bubbleVertImage.draw(Main.screen, x,           y + h - 4 - 16, false, false, true); 
            Common.bubbleVertImage.draw(Main.screen, x + w - 4,   y + h - 4 - 16, true, false, true); 
        }
        
        // Draw corners
        Common.bubbleCornerImage.draw(Main.screen, x,  y, false, false, true);
        Common.bubbleCornerImage.draw(Main.screen, x + w - 4,  y, true, false, true);
        Common.bubbleCornerImage.draw(Main.screen, x,  y + h - 4, false, true, true);
        Common.bubbleCornerImage.draw(Main.screen, x + w - 4,  y + h - 4, true, true, true);
    }
    
    
    public static void drawTextCellCentered( float cellX, float cellY, float cellWidth, String text)    
    {
        float fullTextWidth = Main.screen.textWidth(text);
        float currX = cellX + (cellWidth/2) - (fullTextWidth/2);
        float currY = cellY;
        Main.screen.setTextPosition( currX+1, currY+1 );
        Main.screen.textColor = 1;
        Main.screen.print( text );
        Main.screen.setTextPosition( currX, currY );
        Main.screen.textColor = 3;
        Main.screen.print(  text);
        
    }
    
    public static void drawPlainTextCellCentered( float cellX, float cellY, float cellWidth, String text, int color)    
    {
        float fullTextWidth = Main.screen.textWidth(text);
        float currX = cellX + (cellWidth/2) - (fullTextWidth/2);
        float currY = cellY;
        Main.screen.setTextPosition( currX, currY );
        Main.screen.textColor = color;
        Main.screen.print( text);
        
    }
    
    public static void drawButtonAndLabel( float winX, float winY, float cellWidth, String text, String buttonName)    
    {
        Main.drawTextCellCentered( winX, winY, cellWidth, text );
        
        // Button
        float fullTextWidth = Main.screen.textWidth(text);
        float currX = winX + (cellWidth/2) - (fullTextWidth/2);
        float currY = winY;
        Common.uiButtonImage.draw( Main.screen, (int)(currX-4), currY - 3, false, false, true );
        Main.screen.setTextPosition( currX+1, currY+1 );
        Main.screen.textColor = 1;
        Main.screen.print( buttonName );
        Main.screen.setTextPosition( currX, currY );
        Main.screen.textColor = 3;
        Main.screen.print( buttonName );
    }

    public static void drawBackStoryWindow(int textNum)    
    {
        // Draw bubble
        drawBubble(5,5,210,120, 145);
        
        // Draw tip
        //Common.bubbleTipImage.draw(Main.screen, 110,  5 + 120 - 4, true, false, true);
        
        // Draw Matti
        Common.mattiImage.draw(Main.screen, 220-Common.mattiImage.width(),  176-Common.mattiImage.height(), false, false, true);
        
        int currX = 10; 
        int currY = 10;
        int incY = 10;
        Main.screen.textColor = 7;
        Main.screen.setTextPosition( currX, currY );
        
        if(textNum==0)
        {
              //               123456789#123456789#123456789#12345    
            Main.screen.print("The climate change has destroyed");  currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("all except one coffee farm in");  currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("the world. Unfortunately that is");   currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("located on an ancient Sami");       currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("graveyard. In the daytime, you must");      currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("collect the beans and escape the"); currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("skeleton army. At night, you have");  currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("to protect your coffee storage from");   currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("people which have been turned into");    currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("zombies because of their");    currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("coffee-addiction.");    currY += incY; Main.screen.setTextPosition( currX, currY );
        }
        else if(textNum==1)
        {
              //               123456789#123456789#123456789#12345    
            Main.screen.print("V" + Common.VERSION_STRING + ", " + Common.DATE_STRING +  ", Hannu Viitala");  currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("");  currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.textColor = 10;
            Main.screen.print("Coding by @Hanski.");  currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("Graphics by @Hanski, @Vampirics,");   currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("Bagzie.");       currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("Sounds by Missozzy, Breviceps,");      currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("DRFX, lewfres."); currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("");  currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.textColor = 12;
            Main.screen.print("Greetings to the best community");  currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("ever, the Pokitto Forum!");   currY += incY; Main.screen.setTextPosition( currX, currY );
            Main.screen.print("Cheers, Matti");   currY += incY; Main.screen.setTextPosition( currX, currY );
        }
    }

}


