package com.tiggerbiggo.prima.processing.fragment;

import com.tiggerbiggo.prima.core.Vector2;
import com.tiggerbiggo.prima.exception.IllegalMapSizeException;
import com.tiggerbiggo.prima.graphics.Gradient;

import java.awt.*;

public class RenderFragment implements Fragment<Color[]>
{
    Fragment<Vector2> in;
    int num;
    Gradient g;

    public RenderFragment(Fragment<Vector2> in, int num, Gradient g)
    {
        if(num <=0) throw new IllegalArgumentException("Number of frames cannot be null");

        this.in = in;
        this.num = num;
        this.g = g;
    }

    public RenderFragment(Fragment<Vector2> in)
    {
        this(in, 1, new Gradient());
    }


    public void setGradient(Gradient g)
    {
        if(g != null)
            this.g = g;
    }

    @Override
    public Color[] get() {
        double base = in.get().magnitude();
        Color[] cA = new Color[num];

        double increment = 1.0f/num;

        for(int i=0; i<num; i++)
        {
            cA[i] = g.evaluate(base + (i*increment));
        }

        return cA;
    }

    @Override
    public Fragment<Color[]>[][] build(Vector2 dims) throws IllegalMapSizeException {
        Fragment<Vector2>[][] map;
        try {
            map = in.build(dims);
        }
        catch(IllegalMapSizeException ex)
        {
            throw ex;
        }

        if(Fragment.checkArrayDims(map, dims))
        {
            RenderFragment[][] thisArray = new RenderFragment[dims.iX()][dims.iY()];
            for(int i=0; i<dims.iX(); i++)
            {
                for(int j=0; j<dims.iY(); j++)
                {
                    thisArray[i][j] = new RenderFragment(map[i][j], num, g);
                }
            }
            return thisArray;
        }
        else throw new IllegalMapSizeException();
    }
}
