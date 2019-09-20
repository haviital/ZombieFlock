import femto.Sprite;
import femto.mode.HiRes16Color;
import femto.input.Button;

import Common;

class HeroEntity extends Hero 
{
   
    HeroEntity(MainDay mainDayPar) 
    { 
        velX = 2;
        velY = 2 + (Common.currentDay * 0.3 );
        halfWidth = width() / 2;
        halfHeight = height() / 2;
        x = 110;
        y = 88;
        //y = 46*16; //!!HV
        beansImage = new Beans();
        crashSprite = new Crash();
        crashSprite.run();
        mainDay = mainDayPar;
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
        
       // check checkCollision
        checkCollision();
        
    }

    void checkCollision()
    {
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
        }
        
        // Collision to tree.
        if(isTilechanged && tileId == Common.TREE_MAP_TILE_ID)
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
        float objX = Common.enemies[0].x + 2;
        float objY = Common.enemies[0].y + 2;
        float objW = Common.enemies[0].width()-4;
        float objH = Common.enemies[0].height()-4;
        float heroX2 = x;
        float heroY2 = y;
        float heroW2 = width();
        float heroH2 = height();
        boolean isIntersection = 
            (objX < heroX2 + heroW2) && 
            (objY < heroY2 + heroH2) &&
            (objX + objW > heroX2) && 
            (objY + objH > heroY2);
                
        if(isIntersection)
        {
            float halfTilemapHeight = (Common.tilemap.height() / 2) *16;
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
                // Collision to castle. Going to checkpoint mode.
                mainDay.continueAt = 0; // no time yet
                mainDay.dialogBeanCount = 0;
                mainDay.state = mainDay.STATE_IN_CHECKPOINT;
                //System.out.println("isIntersection = true");
            }
            else
            {
                // Won the day field!
                mainDay.isWon = true;
                Common.totalBeanCount += mainDay.currentBeanCount;
            }
        }

        // Store prev values 
        prevTileID = tileId;
   }
    
    void drawMe(HiRes16Color screen)
    {
        float previousY = y;
        draw(screen, x, y );
        y = previousY;
    }
    
    public float velX;
    public float velY;
    public float halfWidth;
    public float halfHeight;
    Beans beansImage;
    Crash crashSprite;
    long stopUntilTime;
    MainDay mainDay;
    int prevTileID;
    float prevY;

}

