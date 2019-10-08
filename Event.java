import femto.mode.HiRes16Color;
import femto.sound.Procedural;
import femto.Image;
import femto.Sprite;

import Common;

class Event
{
    // Event types
    static final int EVENT_NONE = 0;
    static final int EVENT_START_BUBBLE = 1;
    static final int EVENT_END_BUBBLE = 2;
    static final int EVENT_SFX = 3;
    static final int EVENT_RANDOM_REPEATABLE_SFX = 4;
    static final int EVENT_START_ZOMBIE_SPIRAL = 5;
    static final int EVENT_END_ZOMBIE_SPIRAL = 6;
    static final int EVENT_START_TUTORIAL_BUBBLE = 7;
    static final int EVENT_END_TUTORIAL_BUBBLE = 8;
    static final int EVENT_START_MOVE_ANIM = 9;
    static final int EVENT_END_MOVE_ANIM = 10;
    
    // Gfx primitives 
    static final int GFX_PRIMITIVE_NONE = 0;
    static final int GFX_PRIMITIVE_RECT = 1;
    
    // enum EventState
    // {
    //     PENDING, RUNNING
    // }
    
    Event() 
    {
        waitingForEvent = EVENT_NONE;
    }
    
    public boolean isFree() 
    {
        return( eventTime == 0 );
;    }
    
    public void clear() 
    {
        eventTime = 0;
        waitingForEvent = EVENT_NONE;
        isDrawingState = false;
    }
    
    public void setBubbleTextEvent(long startTime, String textLinePar1, String textLinePar2, Bubble bubbleImagePar ) 
    {
        System.out.println("wait: EVENT_START_BUBBLE");
        eventTime = Common.currentFrameStartTimeMs + startTime;
        waitingForEvent = EVENT_START_BUBBLE;
        isDrawingState = false;  
        image = bubbleImagePar;
        textLineArray1[] = new String[1];
        textLineArray2[] = new String[1];
        textLineArray1[0] = textLinePar1;
        textLineArray2[0] = textLinePar2;
    }
    
    public void setTutorialBubbleEvent(long startTime, Bubble bubbleImagePar, 
            String textLineArrayPar1[], String textLineArrayPar2[], int arrCountPar) 
    {
        System.out.println("wait: EVENT_START_TUTORIAL_BUBBLE");
        eventTime = Common.currentFrameStartTimeMs + startTime;
        waitingForEvent = EVENT_START_TUTORIAL_BUBBLE;
        isDrawingState = false;  
        image = bubbleImagePar;
        arrCount = arrCountPar;
        textLineArray1 = textLineArrayPar1;
        textLineArray2 = textLineArrayPar2;
    }
    
    public void setSfxEvent(long startTime, Procedural sfxPar, float minRandPeriodPar, float maxRandPeriodPar) 
    {
        eventTime = Common.currentFrameStartTimeMs + startTime;
        sfx = sfxPar;
        
        if(minRandPeriodPar>0)
        {
            System.out.println("wait: EVENT_RANDOM_REPEATABLE_SFX");
            waitingForEvent = EVENT_RANDOM_REPEATABLE_SFX;
            minRandPeriod = minRandPeriodPar;
            maxRandPeriod = maxRandPeriodPar;
        }
        else
        {
            System.out.println("wait: EVENT_SFX");
            waitingForEvent = EVENT_SFX;
        }
    }
    
    public void setSpiralAnimationEffect(long startTime, Sprite zombie, float x, float y)
    {
        clear();
        eventTime = Common.currentFrameStartTimeMs + startTime;
        waitingForEvent = EVENT_START_ZOMBIE_SPIRAL;
        sprite = zombie;
        posX = x;
        posY = y;
        System.out.println("setSpiralAnimationEffect((): currentFrameStartTimeMs=" + Common.currentFrameStartTimeMs);
        System.out.println("setSpiralAnimationEffect((): eventTime=" + eventTime);
    }
    
    public void setMoveAnimEvent(long startTime, long endTime, float startX, float startY, float endX, float endY,
            Image imagePar, Sprite spritePar, boolean drawRelativeToHeroPar, float shakeXPar, float shakeYPar)
    {
        System.out.println("wait: EVENT_START_MOVE_ANIM");
        clear();
        eventTime = Common.currentFrameStartTimeMs + startTime;
        startEventTime = eventTime;
        endEventTime = Common.currentFrameStartTimeMs + endTime;
        waitingForEvent = EVENT_START_MOVE_ANIM;
        image = imagePar;
        sprite = spritePar;
        posX = startX;
        posY = startY;
        targetPosX = endX;
        targetPosY = endY;
        drawRelativeToHero = drawRelativeToHeroPar;
        shakeX = shakeXPar;
        shakeY = shakeYPar;
    }

    public void setGfxPrimitiveMoveAnimEvent(long startTime, long endTime, float startX, float startY, float endX, float endY,
            long gfxPrimitivePar, long widthPar, long heightPar, long colorIndexPar, boolean drawRelativeToHeroPar)
    {
        System.out.println("wait: EVENT_START_MOVE_ANIM");
        clear();
        eventTime = Common.currentFrameStartTimeMs + startTime;
        startEventTime = eventTime;
        endEventTime = Common.currentFrameStartTimeMs + endTime;
        waitingForEvent = EVENT_START_MOVE_ANIM;
        image = null;
        sprite = null;
        gfxPrimitive = gfxPrimitivePar;
        posX = startX;
        posY = startY;
        targetPosX = endX;
        targetPosY = endY;
        drawRelativeToHero = drawRelativeToHeroPar;
        width = widthPar;
        height = heightPar;
        colorIndex = colorIndexPar;
    }

    public void execute()
    {
        if( waitingForEvent == EVENT_START_BUBBLE )
        {
            System.out.println("EVENT_START_BUBBLE");
            eventTime = Common.currentFrameStartTimeMs + (long)(2*1000);
            waitingForEvent = EVENT_END_BUBBLE;
            isDrawingState = true; 
        }
        else if( waitingForEvent == EVENT_END_BUBBLE )
        {
            System.out.println("EVENT_END_BUBBLE");
            clear();    
        }
        else if( waitingForEvent == EVENT_START_TUTORIAL_BUBBLE )
        {
            System.out.println("EVENT_START_TUTORIAL_BUBBLE");
            eventTime = Common.currentFrameStartTimeMs + (long)(1000*1000); // no timed stop
            waitingForEvent = EVENT_END_TUTORIAL_BUBBLE;
            isDrawingState = true; 
            phase = 0;
            phaseTimePeriod = (long)(2*1000);
            phaseEndTime = Common.currentFrameStartTimeMs + phaseTimePeriod;
            phaseCheckPointTimePeriod = (long)(2*1000);
            phaseCheckPointTime = Common.currentFrameStartTimeMs + phaseCheckPointTimePeriod;
            lastPhase = arrCount-1; // the rest are random thoughts
            
            // Random thoughts?
            if(Math.random( 0, 30 ) == 1)
            {
                //int thoughtNum = Math.random( 0, 3 );
                //lastPhase = 0;  // Only one bubble.
                
                // Copy random a thought
                //textLineArray1[0] = textLineArray1[arrCount/2 + thoughtNum];
                //textLineArray2[0] = textLineArray2[arrCount/2 + thoughtNum];
           }
        }
       else if( waitingForEvent == EVENT_SFX )
        {
            System.out.println("EVENT_SFX");
            sfx.play();
            clear();      
        }
        else if( waitingForEvent == EVENT_RANDOM_REPEATABLE_SFX )
        {
            System.out.println("EVENT_RANDOM_REPEATABLE_SFX");
            // Play sound.
            //System.out.println("Event.execute(). play sfx");
            sfx.play();
            
            // Rattle a new sfx time, and relaunch the event.
            float startTime = ( Math.random() * (maxRandPeriod-minRandPeriod) ) + minRandPeriod;
            setSfxEvent((int)startTime, sfx, minRandPeriod, maxRandPeriod);
        }
        else if( waitingForEvent == EVENT_START_ZOMBIE_SPIRAL )
        {
            eventTime = Common.currentFrameStartTimeMs + (long)(1000*1000); // no timed stop
            waitingForEvent = EVENT_END_ZOMBIE_SPIRAL;
            isDrawingState = true; 
            phase = 0;
            lastPhase = 3;
            phaseEndTime = Common.currentFrameStartTimeMs + (long)(500);
            System.out.println("execute(): currentFrameStartTimeMs=" + Common.currentFrameStartTimeMs);
            System.out.println("execute(): phaseEndTime=" + phaseEndTime);
        }
        else if( waitingForEvent == EVENT_START_MOVE_ANIM )
        {
            System.out.println("EVENT_START_MOVE_ANIM");
            eventTime = endEventTime;
            waitingForEvent = EVENT_END_MOVE_ANIM;
            isDrawingState = true; 
        }
        else if( waitingForEvent == EVENT_END_MOVE_ANIM )
        {
            System.out.println("EVENT_END_MOVE_ANIM");
            clear();      
        }
    }
    
    public void draw(HiRes16Color screen)
    {
        if( waitingForEvent == EVENT_END_BUBBLE )
        {
            // Draw bubble.
            image.draw(screen, 100, 60);
            screen.setTextPosition( 100+5, 60+5 );
            screen.textColor = 0;
            screen.print(textLineArray1[0]);
            screen.setTextPosition( 100+5, 60+5+8 );
            screen.print(textLineArray2[0] );
         }
        else if( waitingForEvent == EVENT_END_ZOMBIE_SPIRAL )
        {
            if(phaseEndTime<Common.currentFrameStartTimeMs)
            {
                phase++;
                phaseEndTime = Common.currentFrameStartTimeMs + (long)(500);
                //!!HV TEST
                System.out.println("phase++ ***********");
                if(phase>=lastPhase)
                {
                    clear(); // end event
                    System.out.println("end ***********");
                }
            }
            
                
            // !!HV TEST    
            // sprite.setMirrored(false);
            // sprite.setFlipped(false);
            // sprite.draw(screen, posX, posY);
            if(waitingForEvent == EVENT_NONE)
            {
                // Ended.
            }
            else if( phase == 0)
            {
                sprite.setMirrored(false);
                sprite.setFlipped(false);
                sprite.draw(screen, posX, posY);
            }
            else if( phase == 1)
            {
                sprite.setMirrored(true);
                sprite.setFlipped(false);
                sprite.draw(screen, posX, posY);
            }
            else if( phase == 2)
            {
                sprite.setMirrored(false);
                sprite.setFlipped(true);
                sprite.draw(screen, posX, posY);
            }
            else if( phase == 3)
            {
                sprite.setMirrored(true);
                sprite.setFlipped(false);
                sprite.draw(screen, posX, posY);
            }
        }
        else if( waitingForEvent == EVENT_END_MOVE_ANIM )
        {
            // Draw image.
            float fulltime = endEventTime - startEventTime;
            float passedTime = Common.currentFrameStartTimeMs - startEventTime;
            float factor = passedTime / fulltime;
            float currX = (targetPosX-posX)*factor + posX;
            float currY = (targetPosY-posY)*factor + posY;
            if(shakeX != 0)
                currX += (Math.random() * shakeX*2) - shakeX;
            if(shakeY != 0)
                currY += (Math.random() * shakeY*2) - shakeY;
            
            // Draw relative to Hero? 
            if(drawRelativeToHero)
            {
                currX += Common.heroEntity.x;
                currY += Common.heroEntity.y;
            }
            
            if(image!=null)
                image.draw(screen, currX, currY);
            else if(sprite!=null)
                sprite.draw(screen, currX, currY);
            else if( gfxPrimitive == GFX_PRIMITIVE_RECT )
                screen.fillRect( currX, currY, currX+width, currY+height, colorIndex, true );
        }
        else if( waitingForEvent == EVENT_END_TUTORIAL_BUBBLE )
        {
            if(phaseEndTime<Common.currentFrameStartTimeMs)
            {
                phase++;
                phaseEndTime = Common.currentFrameStartTimeMs + phaseTimePeriod;
                System.out.println("wait: EVENT_END_TUTORIAL_BUBBLE. phase="+phase+", last phase="+lastPhase);
                phaseCheckPointTime = Common.currentFrameStartTimeMs + phaseCheckPointTimePeriod;
                if(phase>lastPhase)
                {
                    System.out.println("wait: EVENT_END_TUTORIAL_BUBBLE. ==> END");
                    clear(); // end event
                }
            }
            
            // Draw bubble until the checkpoint time.
            if(waitingForEvent != EVENT_NONE && phaseCheckPointTime > Common.currentFrameStartTimeMs)
            {
                // Draw bubble.
                float fullTextWidth = Main.screen.textWidth(textLineArray1[phase]);
                float fullTextWidth2 = Main.screen.textWidth(textLineArray2[phase]);
                if( fullTextWidth2 > fullTextWidth )
                    fullTextWidth = fullTextWidth2;
                float bubbleW = fullTextWidth + 10;
                float bubbleX = 220 - bubbleW - 2;
                float bubbleY = 10;
                float bubbleH = 2*8 + 10;
                float tipRelPos = bubbleW-10;
                //System.out.println("fullTextWidth="+fullTextWidth+", fullTextWidth2="+fullTextWidth2+", bubbleW="+bubbleW);
                Main.drawBubble((int)bubbleX, (int)bubbleY, (int)bubbleW, (int)bubbleH, (int)tipRelPos);
                
                // Draw text
                Main.drawPlainTextCellCentered( bubbleX, bubbleY+5,   bubbleW, textLineArray1[phase], 0);
                Main.drawPlainTextCellCentered( bubbleX, bubbleY+5+8, bubbleW, textLineArray2[phase], 0);   

                //public static void drawTextCellCentered( float cellX, float cellY, float cellWidth, String text)    

            
                //image.draw(screen, 100, 60, false, false, true);
                //screen.setTextPosition( 100+5, 60+5 );
                //screen.textColor = 0;
                //screen.print(textLineArray1[phase]);
                //screen.setTextPosition( 100+5, 60+5+8 );
                //screen.print(textLineArray2[phase]);
            }
         }
     }
    
    // Member data
    public long eventTime;
    public int waitingForEvent;
    public boolean isDrawingState;
    //public EventState state;
    Image image;
    Sprite sprite;
    long gfxPrimitive;
    float posX;
    float posY;
    float targetPosX;
    float targetPosY;
    long phaseTimePeriod;
    long phaseEndTime;
    long phaseCheckPointTimePeriod;
    long phaseCheckPointTime;
    int phase;
    int lastPhase;
    long startEventTime;
    long endEventTime;
    boolean drawRelativeToHero;
    long width;
    long height;
    long colorIndex;
    float shakeX;
    float shakeY;

    // Bubble text event specific parameters
    public String[] textLineArray1;
    public String[] textLineArray2;
    int arrCount;
    
    // Sfx event specific parameters
    Procedural sfx;
    float minRandPeriod; 
    float maxRandPeriod;
}

