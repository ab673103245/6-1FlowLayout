package qianfeng.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/26 0026.
 */
public class FlowLayout extends ViewGroup {

    private List<List<View>> mAllViewList = new ArrayList<>(); // 之所以在这里new，而不是在onLayout()里面new，是因为onLayout()这个方法会执行两次
    private List<Integer> perMaxHeightList = new ArrayList<>();


    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) { // 我在这里 getWidth都是父容器的
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT); // 宽度和高度的构造方法
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs); // 这个是父容器的布局参数，使用来接收子控件设置的MarginLeft、MarginTop等的属性的
        // 在View里面得到上下文的方法是：getContext()
    }

    // 下面是重点，怎样测量子控件在父容器中要摆放多少行，每行摆放的最大高度 肯定是 取决于那一行的最高的那个子控件的高度的
    // 父容器的测量肯定是要写在onMeasure()方法里面啦！
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { // 在onMeasure()方法里面是不能调用getWidth()方法的，否则是画不了图的！！
        // 在onDraw()或者onLayout()方法中才可以直接使用getWidth(),在onMeasure()方法里面是不行的！！
        // onLayout()里面的view.layout(),绘制的是子控件啊！注意不要让这个方法绘制marginLeft什么的属性啊，
            //  它只能控制子控件从哪个坐标开始绘制啊！一定要分清楚，它是在绘制子控件啊！！！
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // 父容器的宽度我在布局文件里面写的是match_parent，高度我写的是wrap_content
        int childCount = getChildCount();

        // 我是不是还要获取子控件在xml中设置的的高度和宽度等信息？
        // 要得到子控件在父容器中设置的信息，我是不是还要有一个父容器的布局参数
        // 问题是 我要怎样得到父容器的布局参数？

        // 注意这三行代码的位置，曾经出现过问题，这是修复后的版本
        int totalWidth = 0;  // 记录每一行中已摆放控件的总宽度
        int perLineMaxHeight = 0;  // 记录每一行中已摆放控件的最大高度，即一行的总高度
        int totalHeight = 0; // 还有有这个变量来记录上面所有已摆放控件所占有的总高度，以便于我在父容器中开辟这些px值的高度


        for (int i = 0; i < childCount; i++) {

            // 遍历时，现获取容器中的每一个子控件
            View view = getChildAt(i);
            // 父容器中有这样一个方法，用于得到子控件在父容器上写的布局参数。
            // (测量子控件的大小)
            measureChild(view, widthMeasureSpec, heightMeasureSpec); // widthMeasureSpec:我在这里测量的就是父容器的宽度

            // 拿到子控件的大小,之后是不是要拿到子控件的布局参数？（即在父容器上的布局参数，接下来该怎么办呢？）
            MarginLayoutParams mlp = (MarginLayoutParams) view.getLayoutParams();// 子控件的布局参数拿到之后有什么用？这里要强转是因为所有的布局参数类都继承自MarginLayoutParams类

            // 我在这里就要拿到子控件的高度和宽度了！包括它设置的MarginTop等信息哦，但是这个MarginTop是来自父容器(MarginLayoutParams)的属性
            int childWidth = view.getMeasuredWidth() + mlp.leftMargin + mlp.rightMargin;
            int childHeight = view.getMeasuredHeight() + mlp.topMargin + mlp.bottomMargin;

            // 子控件的信息拿到了，接下来就是和父容器中的宽度信息比较，看一行能摆放多少个，一行的最大高度是什么
            // 那就要用几个变量来记录这些值了，一行摆放的所有控件最大高度，一行能够摆多宽的多少个子控件，要与父容器的宽度值进行比较，


            // 如果是换行，该怎么做呢？该怎么统计上一行的高度呢？换行的标志是什么呢？这个是在父容器中，getWidth其实就是获得父容器的宽度，就是match_parent中的值。
            // 第一行肯定就不是换行了   getWidth()为什么不行? 是因为它会调用到onMeasure里面的方法？而这个值是从MeasuerSpec.getSize()里面拿到的？
            if (totalWidth + childWidth > widthSize)// 首先统计换行的标志，看是否满足换行条件(已摆放的总宽度+将要摆放的子控件的宽度，看是否会超过父容器的宽度，如果超过了，这个子控件就要在下一行的开始位置那里摆放)
            {
                totalHeight += perLineMaxHeight;
                totalWidth = childWidth;
                perLineMaxHeight = childHeight;
            } else  // 不换行的情况
            {
                totalWidth += childWidth;
                perLineMaxHeight = Math.max(perLineMaxHeight, childHeight);
            }
            if (i == childCount - 1)  // (childCount:说明已经到达最后一行的最后一个)最后一行的时候，要统计最后一行的高度！这是因为我在上一个if里面处理的逻辑决定的
            {
                totalHeight += perLineMaxHeight;
            }

            // 看测量模式，决定要怎么设置高度(因为宽度已经设置为match_parent了),高度要设置成什么完全取决于Mode,如果是精确测量模式，就不用管，其他的情况就设置为我们设置的值
            heightSize = heightMode == MeasureSpec.EXACTLY ? heightSize : totalHeight; // (heightSize:就是我父容器的getHeight() 也就是 int heightSize = MeasureSpec.getSize(heightMeasureSpec); )

            // 最终设定父容器的高度和宽度 , 注意两次错误都是使用了getWidth()，等下看看为什么调用这个方法是不行的
            setMeasuredDimension(widthSize,heightSize);

        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) { // 因为这个onLayout()方法会执行两次！！！，所以不能在这里初始化父集合还有记录每行最大高度的集合。
                  // onLayout()方法会执行两次

        // 下面来直接写这个吧，需要两个东西，一个是集合用来存放一个View集合，这个View集合是每一行中要摆放的控件，集合里面嵌套一个集合
        // 还有一个集合是用来记录每行的最大高度，来决定下一行的控件该在什么坐标Top里开始

        // 那么首先我要自己创建出集合吧，而且集合里面还要嵌套集合
        // 注意里面嵌套的集合不要用clear，因为这样只是清除了集合中的数据，你如果要把一个集合中的内容添加进父集合中，用clear的子集合，而不是new的话
        // 最后父集合里面 添加到的 将是同一个子集合，而且这个父集合里只会有子集合中最后一次存储的数据，其他数据全丢了。这就是引用的是同一个子集合实例还是各不相同的子集合实例之间的差别。

        // 现在就是先把数据添加到集合中，再取集合中的数据进行布局

        // 因为onLayout()方法会执行两次，所以我这里先清空上一次mAllViewList中的数据，免得影响第二次调用的onLayout()
        mAllViewList.clear();
        perMaxHeightList.clear();

        // 还是像刚才测量的时候差不多啊！
        int childCount = getChildCount();
        int totalWidth = 0;
        int perMaxHeight = 0;
        List<View> viewList = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();

            int childWidth = view.getMeasuredWidth() + lp.leftMargin + lp.rightMargin; // 从父容器那里设置的布局参数，读取出来，因为这是下一个子控件在哪里布局的重要依据
            int childHeight = view.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;


            // 判断需不需要换行
            if(totalWidth + childWidth > getMeasuredWidth()) // 需要换行
            {
                mAllViewList.add(viewList);  // 需要换行的时候，先把'上一行'的存放一行中可摆放的view的子集合添加进父集合中先,接下来肯定是new一个新的子集合进行继续接收view啦！千万不能clear，因为clear掉之后，上面已经说过原因了！就是引用的是堆中同一个地址和引用堆中不同的地址之间的问题。
                viewList = new ArrayList<>();  // 换一个新的集合进行存储新的一行的数据啦！注意父集合里面存放的子集合是：各子集合的引用
                perMaxHeightList.add(perMaxHeight);  // 把'上一行'的最大高度赋值给这个记录高度的集合

                perMaxHeight = 0;
                totalWidth = 0;
            } // 先写下面3行代码，再写上面这个if换行的代码，就不容易乱

            // 先考虑不需要换行的情况，就是一定会执行的。换了行也一样要继续排布子控件的嘛,所以排放下一个子控件的代码是必须执行的
            perMaxHeight = Math.max(perMaxHeight,childHeight);
            totalWidth += childWidth;
            viewList.add(view); // 这个viewList是存放每一行中要摆放的view的，所以一定要添加进这里来啦

            // 还是要最后一行的时候执行最后一行的view的添加及该行控件的最大高度的记录（不过这次是用集合来记录）。
//            if(i == childCount - 1) // 这几行代码可以用下面的两行代码解决，出了for循环之后，就代表最后一次的控件统计完了，
//                                        也统计完高度了，但是没有放到存储它们的集合中去. 所以可以在除了for循环之后把数据各自添加进各自的集合中。注意考虑最后一次的问题
//            {
//                mAllViewList.add(viewList);
//                perMaxHeightList.add(perMaxHeight);
//            }

        }
        // 最后一次的数据，也添加进集合中了。注意换行里面每次添加的都是‘上一行的数据’，而最后一行没有下一行，自然就没有进入那个换行的if代码里
        mAllViewList.add(viewList);
        perMaxHeightList.add(perMaxHeight);

        Log.d("google-my:", "onLayout: 集合大小:" + mAllViewList.size()); // 调试看看集合中的数据对不对

        // 集合在上面已经拿到了，接下来就是对这个集合中的数据进行布局。onLayout()的核心代码：view.layout()
        int totalLeft = 0; // mleft(也可以起名字为 totalLeft)是已经排布过的控件所占过的位置，是所有已经排过位置的控件的最右边的坐标，即你下一个控件要开始排放的起始坐标！
        int totalTop = 0; // mTop是

        for (int i = 0; i < mAllViewList.size(); i++) {
            List<View> viewList1 = mAllViewList.get(i);
            for (int j = 0; j < viewList1.size(); j++) {

                View view = viewList1.get(j);
                MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();

                int left = totalLeft + lp.leftMargin;// 控件起始绘制的坐标,是不理边距的！卧槽，边距是父容器来画的，view只画子控件！！！而且我们的方法的确是只画子控件view啊！！
                int top = totalTop + lp.topMargin;
                int right = left + view.getMeasuredWidth() ; // right只是算到控件的右边，并没有算上右边距。我每次加的依据都是从左边开始算的话，那么就没有实际意义上的右边界啦！因为这个右边界已经被算在子控件所占的控件里了
                int bottom = top + view.getMeasuredHeight();// bottom,只是算到控件的底部，而没有理底部边距

                totalLeft += view.getMeasuredWidth() + lp.leftMargin + lp.rightMargin; // 总的左边距起始(这是一行中的左边距)

                // 这里绘制的是view，是子控件。父容器margin是父容器里面给的，子控件的绘制不可能能绘制父容器的属性，子控件只是决定在哪里起始绘制即可。
                view.layout(left,top,right,bottom); // 这个是核心代码啊！子控件该放在哪里，调用view.layout();在里面设置坐标,设置上下左右，
                            // 其实我只要记录左边还有上面的坐标就可以了，右边和底部的坐标都可以根据左边和上边的坐标来进行计算。
            }

            // totalTop:应该在一行的view遍历结束后，再赋值的
            totalTop += perMaxHeightList.get(i);

            totalLeft = 0; // 结束一行的绘制之后，总的左边的起始位置要重置为 0。

        }



    }

}
