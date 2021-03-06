package com.tiggerbiggo.prima.core;
import com.tiggerbiggo.prima.processing.fragment.Fragment;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Stack;

public class Builder implements Runnable
{
    public static final int THREADNUM = 6;

    private Thread[] threads;
    private boolean setup = false;
    private boolean isDone = false;
    private Stack<Vector2> fragList;
    private Fragment<Color[]>[][] fragMap;
    private BufferedImage[] imgs = null;
    private int w, h, n;
    private int current, max;

    public Builder(Fragment<Color[]>[][] fragMap)
    {
        try {
            if (fragMap == null || fragMap.length <= 0 || fragMap[0].length <= 0) {
                throw new IllegalArgumentException("Invalid FragMap");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new IllegalArgumentException("FragMap is either null or otherwise invalid");
        }
        fragList = new Stack<>();

        this.fragMap = fragMap;

        w = fragMap.length;
        h = fragMap[0].length;
        n = fragMap[0][0].get().length;

        for(int i=0; i<w; i++)
            for (int j=0; j<h; j++)
                fragList.add(new Vector2(i, j));

        max = fragList.size();
        current = 0;

        Collections.shuffle(fragList); //optional

        imgs = new BufferedImage[n];
        for(int i=0; i<n; i++)
        {
            imgs[i] = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        }
    }

    public void callback()
    {}

    public void startBuild()
    {
        threads = new Thread[THREADNUM];
        for(int i=0; i<THREADNUM; i++)
        {
            threads[i] = new Thread(this);
        }
        setup = true;
        for(Thread t : threads)
        {
            t.start();
        }
    }

    @Override
    public void run() {
        if(setup)
        {
            Vector2 pos = getNext();
            while(pos != null) {
                current++;

                if(current % 50000 == 0)
                    System.out.print(".");

                Color[] colors = fragMap[pos.iX()][pos.iY()].get();
                if(colors.length != n)
                {
                    break;
                }
                else
                {
                    for(int i=0; i<n; i++)
                    {
                        imgs[i].setRGB(pos.iX(), pos.iY(), colors[i].getRGB());
                    }
                }
                pos = getNext();
                callback();
            }
        }
    }

    private synchronized Vector2 getNext()
    {
        if(fragList.isEmpty())
            return null;
        return fragList.pop();
    }

    public int getCurrent()
    {
        return current;
    }

    /**Joins all currently working threads in this object to the thread that called this method.
     * This effectively results in the thread waiting until the build operation has completed.
     */
    public void joinAll()
    {
        if(setup)
        {
            for(Thread t : threads)
            {
                try {
                    t.join();
                }
                catch(InterruptedException e) { }
            }
        }
    }

    public boolean isDone()
    {
        if (!isDone) {
            if (!setup) {
                return false;
            }
            for (int i = 0; i < THREADNUM; i++) {
                try {
                    if (threads[i].isAlive())
                        return false;
                } catch (Exception e) {
                    return false;
                }
            }
            isDone = true;
            return true;
        }
        else return true;
    }

    public BufferedImage[] getImgs() {
        return imgs;
    }
}
