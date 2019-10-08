import femto.Sprite;
import femto.mode.HiRes16Color;
import femto.input.Button;

import Common;

class RunSfx extends drfx__soft_grass_foot_step {
  float time;
  public float speed = 1.5;
  
  RunSfx(char channel)
  {
      super(channel);
  }

  public void reset(){
    time = 0;
    super.reset();
  }

  ubyte update(){
    time += speed;
    t = (int) time; // "t" is the current sample in Bark. Maybe add Math.round?
    return super.update();
  }
}

class HeroEntity extends Hero 
{
   
    static final long DUST_COUNT = 8;
    static final long DUST_PERIOD_MS = 75;
   
    public float velX;
    public float velY;
    public float halfWidth;
    public float halfHeight;
    Beans beansImage;
    Crash crashSprite;
    Dust[] dustArray;
    long currenDustIndex;
    long dustTime;
    long stopUntilTime;
    MainDay mainDay;
    int prevTileID;
    float prevY;
    float dustHalfWidth;
    long runSfxReplayTimeAt;
    
    // Sounds
    RunSfx runSfx;
    breviceps__step_into_water_puddle_wade pickBeanSfx;

    HeroEntity(MainDay mainDayPar) 
    { 
        velX = 2;
        velY = 2 + (Common.currentDay * 0.3 );
        if( Common.isTutorialActive )
            velY /= 2;  // Slow down in tutorial mode.
        halfWidth = width() / 2;
        halfHeight = height() / 2;
        x = 110;
        y = 88;
        //y = 46*16; //!!HV
        beansImage = new Beans();
        crashSprite = new Crash();
        crashSprite.run();
        mainDay = mainDayPar;
        dustArray = new Dust[DUST_COUNT]; //
        for( int i=0; i < DUST_COUNT; i++ )
            dustArray[i] = new Dust();
        currenDustIndex = 0;
        dustHalfWidth = dustArray[0].width() / 2;
        
        // Sfx
        runSfx  = new RunSfx(1);
        pickBeanSfx  = new breviceps__step_into_water_puddle_wade(1);
    }
    
    void update(HiRes16Color screen)
    {
        float previousX = x;
        float previousY = y;
        
        // Move the bar.
        if( Button.Left.isPressed() )
        {
            x -= velX;
        }
        else if( Button.Right.isPressed() )
        {
            x += velX;
        }
            
            
        // Always go forward, if not stopped
        if(stopUntilTime<Common.currentFrameStartTimeMs)
            y += velY;
        
        //if( Button.Up.isPressed() )
        //{
        //    y -= velY;
        //    
        //    for(int i=0; i < Common.MAX_ENEMIES; i++)
        //        Common.enemies[i].y -= velY;
        //}
        //else if( Button.Down.isPressed() )
        //{
        //    y += velY;
        //    
        //    for(int i=0; i < Common.MAX_ENEMIES; i++)
        //        Common.enemies[i].y += velY;
        //}
        
        // Dust
        if( dustTime < Common.currentFrameStartTimeMs )
        {
            currenDustIndex += 1;
            if( currenDustIndex >= DUST_COUNT)
                currenDustIndex = 0;
            dustArray[currenDustIndex].x = x +  halfWidth - dustHalfWidth;
            dustArray[currenDustIndex].y = y + height() - dustArray[currenDustIndex].height();
            dustArray[currenDustIndex].endFrame = 0;  // Causes reset in run()
            dustArray[currenDustIndex].run();
            dustTime = Common.currentFrameStartTimeMs + DUST_PERIOD_MS;
        }
        
       // check checkCollision
        checkCollision();
        
        // Play sfx
        if(y > prevY && runSfxReplayTimeAt < Common.currentFrameStartTimeMs)
        {
            runSfx.speed = (Math.random() * 2.0) + 0.5; // 0.5 - 1.5
            runSfx.play();
            runSfxReplayTimeAt = Common.currentFrameStartTimeMs + 230;
        }
    }

    void checkCollision()
    {
        // Check out of tilemap end
        if( y > Common.tilemap.height()*16)
        {
            // End the day level
            
            // Collected beans are lost! Only stored beans are counted.
            mainDay.currentBeanCount = mainDay.storedBeanCount;
            
            // How much bean stored in the tower?
            if(mainDay.currentBeanCount<40)
            {
                // Not enough beans!
                mainDay.isGameOver = true;
            }
            else
            {
                // Won the day field!
                mainDay.isWon = true;
                Common.totalBeanCount = mainDay.currentBeanCount;
            }
        
        }

        // Check out of screen edges.
        if( x < 0 )
            x = 0;
        if( x > 220 - width() )
            x = 220 - width();

        float heroX = x + halfWidth;
        int tileXIndex = ((int)heroX) >> 4;
        float heroY = y + halfHeight;
        int tileYIndex = ((int)heroY) >> 4;
        
        // Collision to beans.
        int tileId = Common.tilemap.readDayField(tileXIndex, tileYIndex);
        boolean isTilechanged = (tileId != prevTileID);

        if(isTilechanged && tileId == Common.BEAN_MAP_TILE_ID)
        {
            // Collect beans.
            //Common.events[0].setMoveAnimEvent(0, (long)1*1000, heroX, heroY, 110.90, -10.0, beansImage );
            Common.events[Main.getNextFreeEvent()].setMoveAnimEvent(0, (long)500, heroX, heroY, 110, heroY-88-20, beansImage, null, false, 0, 0 );
            //System.out.println("CRASH!");
            mainDay.currentBeanCount += 10;
            pickBeanSfx.play();
        }
        
        float heroX2 = x;
        float heroY2 = y;
        float heroW2 = width();
        float heroH2 = height();
        float objX;
        float objY;
        float objW;
        float objH;
        boolean isIntersection;
        
        boolean isCollidedToBatsOrBushes = (isTilechanged && tileId == Common.TREE_MAP_TILE_ID);
        if( ! isCollidedToBatsOrBushes )
        {
            // Collision to bats.
            for(int i=0; i < Common.MAX_BATS; i++)
            {
                
                if( ! mainDay.bats[i].isHit )
                {
                    //bats[i].drawMe(Main.screen);
                    // skeleton rect
                    objX = mainDay.bats[i].x + 2;
                    objY = mainDay.bats[i].y + 2;
                    objW = mainDay.bats[i].width()-4;
                    objH = mainDay.bats[i].height()-4;
                    boolean isIntersection = 
                        (objX < heroX2 + heroW2) && 
                        (objY < heroY2 + heroH2) &&
                        (objX + objW > heroX2) && 
                        (objY + objH > heroY2);
                            
                    if(isIntersection)
                    {
                        isCollidedToBatsOrBushes = true;
                        mainDay.bats[i].isHit = true;
                    }
                }
            }
        }
        
        // Collision to bat or bush.
        if(isCollidedToBatsOrBushes)
        {
            // Slow down.
            Common.events[Main.getNextFreeEvent()].setMoveAnimEvent(0, (long)500, 0, -10, 0, -10, null, crashSprite, true, 0, 0 );
            System.out.println("CRASH TREE!");
            stopUntilTime = Common.currentFrameStartTimeMs + 100;
            
            // Decrease distance to enemies
            //y -= (3 + Common.currentDay);
            for(int i=0; i < Common.MAX_ENEMIES; i++)
                Common.enemies[i].followHeroOffsetY += (3 + Common.currentDay);

        }

        // Collision to the first skeleton.
        
        // skeleton rect
        objX = Common.enemies[0].x + 2;
        objY = Common.enemies[0].y + 2;
        objW = Common.enemies[0].width()-4;
        objH = Common.enemies[0].height()-4;
        isIntersection = 
            (objX < heroX2 + heroW2) && 
            (objY < heroY2 + heroH2) &&
            (objX + objW > heroX2) && 
            (objY + objH > heroY2);
                
        if(isIntersection)
        {
            mainDay.isGameOver = true;
        }

        // Check collision to castle
        
        // Castle rect
        objX = mainDay.castleX + 3;
        objY = mainDay.castleY + 3;
        objW = mainDay.castleDayImage.width()-6;
        objH = mainDay.castleDayImage.height()-6;
        isIntersection = 
            (objX < heroX2 + heroW2) && 
            (objY < heroY2 + heroH2) &&
            (objX + objW > heroX2) && 
            (objY + objH > heroY2);
                
        if(isIntersection)
        {
            float halfTilemapHeight = (Common.tilemap.height() / 2) *16;

            if(heroY < halfTilemapHeight)
            {
                // First tower
                
                // Collision to castle. Going to checkpoint mode.
                mainDay.continueAt = 0; // no time yet
                mainDay.storedBeanCount = 0;
                mainDay.state = mainDay.STATE_IN_CHECKPOINT;
                //System.out.println("isIntersection = true");
            }
            else
            {
                // Last tower
                if(mainDay.currentBeanCount<40)
                {
                    // Not enough beans!
                    mainDay.isGameOver = true;
                }
                else
                {
                    // Won the day field!
                    mainDay.isWon = true;
                    Common.totalBeanCount = mainDay.currentBeanCount;
                }
                
            }
        }
        
       // Store prev values 
        prevTileID = tileId;
        
   }
    
    void drawMe(HiRes16Color screen)
    {
        // Draw dust
        for( int i=0; i < DUST_COUNT; i++ )
            dustArray[i].draw(screen);
        
        
        // draw hero
        draw(screen, x, y );
    }
    
}

