import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

class ScreenSelect extends JFrame
{
    // 开始选择截图的位置
    private Point startPoint;
    // 结束选择截图的位置
    private Point endPoint;
    // 截图结果的缓存图像
    private BufferedImage screenshotImage;
    // 选择区域按钮
    private JButton selectRegionButton;
    // 截图按钮
    private JButton captureButton;
    // 是否正在选择截图区域的标志
    private boolean isSelectingArea;

    public ScreenSelect()
    {
        setTitle("自选区域截图");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(true); // 设置窗口为无边框模式
        setOpacity(0.5f); // 设置窗口透明度

        // 获取所有可用的屏幕设备
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();

        // 计算所有屏幕的组合尺寸
        Rectangle allScreensBounds = new Rectangle();
        for (GraphicsDevice screen : screens) {
            GraphicsConfiguration config = screen.getDefaultConfiguration();
            allScreensBounds = allScreensBounds.union(config.getBounds());
        }

        // 将窗口大小设置为覆盖所有屏幕
        setSize(allScreensBounds.width, allScreensBounds.height);

        isSelectingArea = true; // 设置为正在选择区域状态
        startPoint = null; // 清空起点坐标
        endPoint = null; // 清空终点坐标
        repaint(); // 重新绘制窗口

        // 添加鼠标事件监听器，以便用户选择截图区域
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (isSelectingArea)
                {
                    startPoint = e.getPoint(); // 记录鼠标按下的位置
                }
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (isSelectingArea)
                {
                    endPoint = e.getPoint(); // 记录鼠标释放的位置
                    repaint(); // 重新绘制窗口，实时更新截图区域的矩形框
                    isSelectingArea = false;
                    // 计算矩形框的左上角坐标
                    int x = Math.min(startPoint.x, endPoint.x);
                    int y = Math.min(startPoint.y, endPoint.y);
                    // 计算矩形框的宽度和高度
                    int width = Math.abs(endPoint.x - startPoint.x);
                    int height = Math.abs(endPoint.y - startPoint.y);
                    Snapper.setStartPoint(startPoint);
                    Snapper.setEndPoint(endPoint);
                    Snapper.log("截取原点："+"("+x+","+y+")");
                    Snapper.log("矩宽："+width+"    矩高："+height);
                    dispose();
                }
            }
        });

        // 添加鼠标移动事件监听器，以便实时绘制截图区域的矩形框
        addMouseMotionListener(new MouseMotionAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent e)
            {
                if (isSelectingArea)
                {
                    endPoint = e.getPoint(); // 记录鼠标拖动的位置
                    repaint(); // 重新绘制窗口，实时更新截图区域的矩形框
                }
            }
        });

        setVisible(true); // 设置窗口可见
    }

    /**
     * 重写 paint 方法，绘制截图区域的矩形框。
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        if (isSelectingArea && startPoint != null && endPoint != null)
        {
            // 计算矩形框的左上角坐标
            int x = Math.min(startPoint.x, endPoint.x);
            int y = Math.min(startPoint.y, endPoint.y);
            // 计算矩形框的宽度和高度
            int width = Math.abs(endPoint.x - startPoint.x);
            int height = Math.abs(endPoint.y - startPoint.y);
            // 绘制矩形框
            g.setColor(Color.RED);
            g.drawRect(x, y, width, height);
        }
    }
}
