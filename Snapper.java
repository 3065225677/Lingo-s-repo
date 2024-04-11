import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*平台页面*/
public class Snapper extends JFrame implements ActionListener
{
    public static void main(String[] args)
    {
        new Snapper();
    }

    // 截图保存路径
    private String storagePath = "D://java_project//snapScreen/dist";

    // 主要显示面板
    private JPanel panel;

    // 窗口大小
    private final int width = 350;
    private final int height = 350;

    // 要截屏的区域与时间间隔
    private Rectangle captureRect = new Rectangle(3, 93, 1591, 899);
    private double shotInterval = 100;

    // 一些特定的交互组件
    private JButton selectAreaButton;
    private JTextField intervalTextField;
    private JButton commitButton;
    private JButton cancelButton;
    private JButton storageButton;
    static private JTextArea logArea;


    // 记录用户开始选择截图的位置
    static private Point startPoint = new Point(2, 93);
    // 记录用户结束选择截图的位置
    static private Point endPoint = new Point(1593, 992);
    // 截图结果的缓存图像
    private BufferedImage screenshotImage;

    private Timer timer = new Timer();

    // 创建一个固定大小的线程池
    ExecutorService executor = Executors.newFixedThreadPool(10);
    private ScheduledExecutorService scheduler;


    public Snapper()
    {
        setTitle("Snapper");
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel()
        {
            @Override
            protected void paintComponent(Graphics g)
            {
                /*super.paintComponent(g);
                if (captureRect != null)
                {
                    g.setColor(Color.RED);
                    g.drawRect(captureRect.x, captureRect.y, captureRect.width, captureRect.height);
                }*/
            }
        }; // 创建面板

        // 设置面板布局
        panel.setLayout(null);

        // 在panel上放置组件
        placeComponents();

        // 把创建出来的面板添加到当前类的内容面板上
        this.getContentPane().add(panel);

        // 设置可见
        setVisible(true);
    }

    private void addComponentToPanel(JComponent component)
    {
        this.panel.add(component);
    }

    // 处理组件放置的逻辑
    private void placeComponents()
    {
        JLabel selectAreaLabel = new JLabel("     选择截取区域");
        addComponentToPanel(selectAreaLabel);
        selectAreaLabel.setBounds(20, 20, 100, 30);

        selectAreaButton = new JButton("选择");
        addComponentToPanel(selectAreaButton);
        selectAreaButton.setBounds(150, 20, 100, 30);
        selectAreaButton.addActionListener(this);

        JLabel setIntervalLabel = new JLabel("设置的截屏时间间隔(s)");
        addComponentToPanel(setIntervalLabel);
        setIntervalLabel.setBounds(20, 70, 130, 30);

        intervalTextField = new JTextField();
        addComponentToPanel(intervalTextField);
        intervalTextField.setBounds(160, 70, 100, 30);

        commitButton = new JButton("开始截屏");
        addComponentToPanel(commitButton);
        commitButton.setBounds(20, 130, 90, 30);
        commitButton.addActionListener(this);

        cancelButton = new JButton("停止");
        addComponentToPanel(cancelButton);
        cancelButton.setBounds(120, 130, 90, 30);
        cancelButton.addActionListener(this);

        storageButton = new JButton("另存至");
        addComponentToPanel(storageButton);
        storageButton.setBounds(220, 130, 90, 30);
        storageButton.addActionListener(this);

        logArea = new JTextArea("日志:\n另存至的路径不会缓存\n每次启动的默认输出路径:\n" + storagePath + "\n");
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea); // 将 JTextArea 放置在 JScrollPane 中
        scrollPane.setBounds(20, 170, 300, 130); // 设置 JScrollPane 的位置和大小
        addComponentToPanel(scrollPane);
    }

    static public void log(String info)
    {
        logArea.append(info + "\n");
    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
        JButton sourceButton = (JButton) e.getSource();
        String buttonName = sourceButton.getText();
        log("点击了按钮：" + buttonName);

        if (sourceButton == commitButton)
        {
            scheduler= Executors.newScheduledThreadPool(10);
            String interval = intervalTextField.getText();
            try
            {
                shotInterval = Double.parseDouble(interval);
            }
            catch (NumberFormatException ex)
            {
                log("请先输入时间间隔后点击");
                return;
            }
            log("截屏间隔:" + interval + " s");

            // 创建新的定时器任务
            Runnable captureTask = () ->
            {
                if (startPoint != null && endPoint != null)
                {
                    // 提交保存图片的操作给线程池执行
                    executor.submit(() -> captureRegion());
                }
                else
                {
                    log("请先选择截图区域");
                }
            };

            // 启动定时器任务
            scheduler.scheduleAtFixedRate(captureTask, 0, (long) (shotInterval * 1000), TimeUnit.MILLISECONDS);

        }
        else if (sourceButton == cancelButton)
        {
            scheduler.shutdown(); // 关闭定时器任务
        }
        else if (sourceButton == selectAreaButton)
        {
            new ScreenSelect();
        }
        else if (sourceButton == storageButton)
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择输出目录");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            // 显示文件选择器对话框
            int result = fileChooser.showOpenDialog(this);

            // 如果用户选择了目录并点击了确定按钮
            if (result == JFileChooser.APPROVE_OPTION)
            {
                File selectedDir = fileChooser.getSelectedFile();
                if (selectedDir.isDirectory())
                {
                    storagePath = selectedDir.getAbsolutePath(); // 获取用户选择的目录路径
                    log("已将输出目录设置为：" + storagePath);
                }
                else
                {
                    log("请选择一个有效的目录");
                }
            }
        }
    }

    /**
     * 截取用户选择的区域，并保存截图结果。
     */
    private void captureRegion()
    {
        int width = Math.abs(endPoint.x - startPoint.x); // 计算选择区域的宽度
        int height = Math.abs(endPoint.y - startPoint.y); // 计算选择区域的高度
        int x = Math.min(startPoint.x, endPoint.x); // 计算选择区域的起始横坐标
        int y = Math.min(startPoint.y, endPoint.y); // 计算选择区域的起始纵坐标
        captureRect = new Rectangle(x, y, width, height);
        try
        {
            // 使用 Robot 类来获取屏幕截图
            Robot robot = new Robot();
            // 创建截图图像对象
            screenshotImage = robot.createScreenCapture(captureRect);
            // 将截图保存到文件
            saveScreenshot(screenshotImage);
        }
        catch (AWTException | IOException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * 将截图结果保存到文件。
     *
     * @param image 要保存的图像
     * @throws IOException 如果保存文件过程中发生 I/O 错误
     */
    private void saveScreenshot(BufferedImage image) throws IOException
    {
        // 生成时间戳作为文件名的一部分，确保文件名唯一
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        // 拼接保存文件的路径
        String fileName = "screenshot_" + timestamp + ".png";
        // 创建要保存的文件对象
        File fileToSave = new File(storagePath + File.separator + fileName);
        // 使用 ImageIO 将图像写入文件
        ImageIO.write(image, "png", fileToSave);
        log("已保存到" + storagePath + "   time:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    static public void setStartPoint(Point point)
    {
        startPoint = point;
    }

    static public void setEndPoint(Point point)
    {
        endPoint = point;
    }

    // 关闭线程池
    @Override
    public void dispose()
    {
        super.dispose();
        executor.shutdown();
        scheduler.shutdown();
    }
}


