package com.myfams.copy; /**
 *   url: 
 *    灰度处理方式主要有三种:

    最大值法: 该过程就是找到每个像素点RGB三个值中最大的值，然后将该值作为该点的
    平均值法：该方法选灰度值等于每个点RGB值相加去平均
    加权平均值法：人眼对RGB颜色的感知并不相同，所以转换的时候需要给予三种颜色不同的权重

背景去除

该过程就是将背景变成纯白色，也就是尽可能的将目标字符之外的颜色变成白色。该阶段最难的就是确定图片的背景和前景的分割点，就是那个临界值。因为要将这张图片中每个像素点R值（灰度处理后的图片RGB的值是相同的）大于临界值的点RGB值都改成255（白色）。而这个临界点在整个处理过程中是不变的。

能区分前景和背景，说明在该分割点下，前景和背景的分别最明显，就像一层玻璃，将河水分成上下两部分，下面沉淀，相对浑浊，上面清澈，这样，两部分区别相当明显。这个片玻璃的所在位置就是关键。
常用临界点阈值确定算法

    双峰法，这种算法很简单，假设该图片只分为前景和背景两部分，所以在灰度分布直方图上，这两部分会都会形成高峰，而两个高峰间的低谷就是图片的前景背景阈值所在。这个算法有局限性，如果该图片的有三种或多种主要颜色，就会形成多个山峰，不好确定目标山谷的所在，尤其是验证码，多种颜色，灰度后也会呈现不同层次的灰度图像。故本程序没有采用这种算法。
    迭代法，该算法是先算出图片的最大灰度和最小灰度，取其平均值作为开始的阈值，然后用该阈值将图片分为前景和背景两部分，在计算这两部分的平均灰度，取平均值作为第二次的阈值，迭代进行，直到本次求出的阈值和上一次的阈值相等，即得到了目标阈值。
    最大类间方差法，简称OTSU，是一种自适应的阈值确定的方法，它是按图像的灰度特性,将图像分成背景和目标2部分。背景和目标之间的类间方差越大,说明构成图像的2部分的差别越大,当部分目标错分为背景或部分背景错分为目标都会导致2部分差别变小。因此,使类间方差最大的分割意味着错分概率最小。而该方法的目标就是找到最符合条件的分割背景和目标的阈值。本程序也是采用的该算法进行背景分离的。
    灰度拉伸算法，这是OTSU的一种改进，因为在前景背景差别不大的时候，OTSU的分离效果就会下降，此时需要增加前景背景的差别，这是只要在原来的灰度级上同时乘以一个系数，从而扩大灰度的级数。

噪点判断及去除

首先是去除边框，有的验证码在图片边界画了一个黑色边框，根据去背景的原理这个边框是没有被去掉的。去除这个边框很简单，对加载到二维数组中每个像素点进行判断，如果该点的横坐标等于0或者图片宽度减一，或者总坐标等于0或者纵坐标等于图片高度减一，它的位置就是边框位置。直接RGB置0去除边框。

对于非边框点，判断该目标像素点是不是噪点不是直接最目标点进行判断的，是观察它周围的点。以这个点为中心的九宫格，即目标点周围有8个像素点，计算这8个点中不是背景点（即白色）点的个数，如果大于给定的界定值（该值和没中验证码图片噪点数目，噪点粘连都有关，不能动态获取，只能根据处理结果对比找到效果好的值），则说明目标点是字符内某个像素点的几率大些，古改点不能作为噪点，否则作为噪点处理掉。假设此次的界定值是2，则：

 
二值化

二值化区别于灰度化，灰度化处理过的图片，每个像素点的RGB值是一样的，在0-255之间，但是二值化要求每个像素点的RGB值不是0就是255.将图片彻底的黑白化。

二值化过程就是对去噪后的验证码图片的每个像素点进行处理，如果该点的R值不是255，那么就将该点的RGB值都改成0（纯黑色），这样整个过程下来，这正图片就变成真正意义上的黑白图片了。

 
图片分割
图片分割的主要算法

图片分割技术在图形图像的处理中占有非常重要的地位，图片是一个复杂的信息传递媒介，相应的，不是每个图片上的所有信息都是预期想要的，因次，在图片上”筛选“出目标区域图像就显得很重要，这就用到了图片分割技术。

图片字符的分割是验证码识别过程中最难的一步，也是决定识别结果的一步。不管多么复杂的验证码只要能准确的切割出来，就都能被识别出来。分割的方式有多种多样，对分割后的精细处理也复杂多样。

下面介绍几种成熟的分割算法：

    基于阈值的分割，这种分割方式在背景处理中已经用到，通过某种方式找到目标图片区域和非目标图片区域间的分界值，进而达到将两个区域分离的目的，这种方式达不到区分每个字符的效果，所以在分割阶段没有采用。
    投影分割，也叫做基于区域的分割，这种分割算法也很简单，就是将二值化后的图片在X轴方向做一次像素点分布的投影，在峰谷的变化中就能定位到每个目标区域了，然后对单个区域进行Y轴投影，进而确定区域位置。该方式对轻微粘连有一定的处理效果，但是，对与噪点会也会产生过分的分割，还有对‘7’，‘T’，‘L’等会产生分割误差，所以，程序中没有采用这种算法。

 

                     图3-7投影法

    边缘检测分割，也叫做点扫描法，这种分割方式能一定程度满足程序的要求，因此，本程序也是采用了这种分割算法，后面会详细介绍。
    聚类，聚类法进行图像分割是将图像空间中的像素用对应的特征空间点表示，根据它们在特征空间的聚集对特征空间进行分割，然后将它们映射回原图像空间，得到分割结果。这种方式处理复杂，但是对粘连，变形等复杂图像处理有良好的效果。由于时间有限，本次课题并没有对该方式进行深入分析实现。

3.6.2边缘检测分割算法

程序采用的是边缘检测的方式确定每个字符边界的。该算法的步骤如下：

 

图3-8图片分割示意图

从图中可以看到，当程序判断”6“这个字符的边界时：

    从扫描指针从图片最左边像素点X轴坐标为0开始，向下扫描，扫描宽度为1px,如果碰到了像素点R值是0的，记下此时X坐标A ，如果扫描到底部都没有遇到，则从指针向右移动一位，继续纵向扫描直到得到A。
    扫描指针从A+1开始，纵向扫描每个像素点，遇到R值是255的，变量K（初始值0）自增一，扫描到底部判断K的值，如果K值等于图片高度，则停止后续扫描，记下此时X轴坐标B，否则指针向右移动一位，继续扫描直到得到B。
    在X区间（A，B-1）中，指针从Y坐标是0点横向扫描，判断每个点的R值，如果R值等于0，则停止扫描，记下此时Y轴坐标C。否则，指着下移一个单位，继续横向扫描
    在X区间（A，B-1），指针从C+1处开始横向扫描，判断每个像素点的R值，如果R值等于255，使N（初始值0）自增一，这行扫描结束后判断N的值，如果该值等于B-A,则停止扫描记下此时的Y轴坐标D，否则指针向下移动以为，继续横向扫描，知道得到D。

“4“这个字符边界的获取也是一样的，只是步骤一中扫描开始的位置X坐标0变成了B+1.

每次判断一下B-A，如果他的值小于你验证码字符中宽度最小的那个，（假设这里定的是4），则停止找边界把坐标加到集合中就可以了。

如学校的验证码字符中，宽度最窄的是1，但它的宽度是大于4的所以该设定没有问题，根据情况来定，一般宽度小于4的，验证码就很小了，不利于人看。

上述过程走完之后，就得到了左右，上下四个边界点的横坐标，纵坐标，即（A，B-1,C ,D-1）;把这四个点确定的区域对应的原验证码所在的区域画到一张小图片上。然后把这张小图片按照设定的高宽进行归一化处理，把处理好的图片放入集合中返回。等待下一步处理。

分割前： 分割后的四张效果：

分割后的特殊处理

在这一过程中，由于图像的部分粘连，往往分割的结果都不会达到预期的效果，分割出的小图片也是千奇百怪。但是，考虑到现在大多数网站的验证码字符都是4个，意味着切割出来的小图片也得是四个，针对这种情况，我就做了进一步处理，首先看下切割后可能出现的情况：

这张验证码是二值化处理过的验证码，很明显，第一个和第二个字符是相互粘连的，利用程序的切割方式切出来的图片应该是3个小图片，类似这样：

显然，①不是程序想要的情况，对于这种情况，即第一次切完是3部分的，就找到最宽的一个，然后从中间剁开。得到4部分图片。

相应的，还有2部分的时候：

这也不是我们理想的情况，也是同样的道理，把两部分中中间剁开，得到4个小图片。

还有这种情况，第一次切割完全是一张的：

我们只需把它均分4分就可以了。

当然上述处理会造成相应的误差，但是只要后面字模数量足够大，这样切割处理效果还是可以的。

此次只对4个字符的情况做了特殊的处理，其他个数的没有做，具体做法会在总结中介绍。

 
字模制作

这个过程是将切割好的图片转化成特征矩阵，把图片切割过程中返回的小图片集合进行特征值获取。在图片切割过程，程序已经将切割好的小图片进行了归一化处理，即长宽都相同，遍历每一个像素，如果该点R值是255，则就记录一个0，如果该点的R值是255，则记录一个1，这样按着顺序，记录好的0，1拼成字符串，这个字符串就是该图片的特征码。然后前面拼上该图片对应的字符，用‘--’连接。这样，一个图片就有一个特征值字符串对应了，把这个特征值字符串写入文本或数据库中，基本的字模库就建立好了。由于图片归一化的时候小图规格是20*30，所以，每个字模数据就是20*30+3+2（回车换行）=605个字符。

字模库的量越大，后面的识别正确率也就越高，但是，并不是越大越好，字模数据越多，比对消耗的时间就越多，相比来说效率就会下降。下面是一张字模库的部分图样：

验证码识别

要想识别验证码，必须要有制作好的字模数据库，然后一次进行下面过程：

    验证码图片的获取，该步骤验证码的来源可以是从网络流中获取验证码，  也可以从磁盘中加载图片。
    图片处理，包括灰度，去噪，去背景，二值化，字符分割，图片归一化，图片特征码获取。

3．计算相似度，读取字模数据库中的字模数据，用归一化后的小图的特征码和字模数据进行对比，并计算相似度，记录相似度最高的字模数据项所对应的字符C。

4．识别结果，依次将所得到的字符C拼接起来，得到的字符串就是该验证码的识别结果。

下面是验证码识别的具体流程：

字模库维护

验证码的识别过程已经详细的分析，识别关键点一个在切割，一个在字模库的质量。字模库涉及两个问题，一个就是重复的问题，一个就是字模数据。这个阶段主要实现：

    重复字模数据的过滤剔除。
    对于插入错误的字模可以进行修改。
    可以删除不需要的字模数据

图片处理类的设计

图像处理类是遵循面向对象的思想设计的，将图像处理过程中用到的方法进行封装，对常用参数值进行参数默认值和可变参数设置，方法重载。该类是静态类，方便开发人员调用，其中Boundry是存储小图片边界信息的类，里面有四个边界值属性。

开发人员可以直接调用GetYZMCode()方法进行验证码的识别处理，这是一个重载方法，其余的方法会在下面具体实现中介绍具体方法的设计，下面是这个类图表示了ImageProcess类中主要的处理方法和之间的关系：

 

发票编号识别

这个是基于aforge.net实现的，参考国外一位扑克牌识别的代码。

过程是先确定发票的位置，然后定位到发票编号，切出发票编号，调用自动识别类库识别数字，然后再将识别数据写到屏幕上。当然也要实现训练字模；

 

完成这个demo过程还是比较有趣，感谢活跃在博客园，csdn，github，开源中国strackoverflow等社区的前辈，他们对开源社区分享，奉献让更多的开发者收益，在他们的肩膀上，我们这些菜鸟才能走的更远。
 */

import com.myfams.Boundary;
import com.myfams.util.HttpUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * 验证码处理
 * @author zhanyf
 *
 */
public class ImageProcessUtil {
	//字模库路径
	private static String zimoFile="c:\\tmp\\zimo\\";
	//验证码路径
	private static String checkCodePath = "c:\\tmp\\checkcode\\";

	public static void main(String[] args){
		
//		for(int i = 0 ;i < 50;i++){
//			saveCheckCodeImg("https://tgfw.csc108.com/cas/servlet/validateCodeServlet?r=0.5437564554169025&phoneValidate=false",
//					HttpUtil.getHttpClient(),"zxzq"+i);
//		}
		
		//rhCheckout();
		//zxzqCheckCode();
		//zqsbCheckCode();
	}

	public static void zqsbCheckCode(){
		System.out.println(getCheckCode("http://kfsjj.stcn.com/CheckCode.aspx?time=Tue%20Feb%2026%202019%2015:41:31%20GMT+0800%20(%E4%B8%AD%E5%9B%BD%E6%A0%87%E5%87%86%E6%97%B6%E9%97%B4)",
				HttpUtil.getHttpClient(),5,"zqsb",190,false,false));
	}

	public static void zxzqCheckCode(){
		System.out.println(getCheckCode("https://tgfw.csc108.com/cas/servlet/validateCodeServlet?r=0.5437564554169025&phoneValidate=false",
				HttpUtil.getHttpClient(),4,"zxzq",155,true,true));
	}

	public static void rhCheckout(){
		System.out.println(getCheckCode("https://ibrs.chinamoney.com.cn/IBRSW/jsps/login/authImg.jsp?token=12312412412341",
				HttpUtil.getHttpClient(),4,"",155,false,false));
	}

	/**
	 * 获取验证码
	 * @param checkCodeUrltemp 验证码地址
	 * @param httpClient httpclient
	 * @param type 类别 中信证券-zxzq 人行-rh 等等
	 * @param charsNum 验证码个数
	 * @Param isSeparate 是否切割图片
	 * @Param isCleanImage 是否清除干扰线
	 * @return
	 */
	public static String getCheckCode(String checkCodeUrltemp,CloseableHttpClient httpClient,int charsNum,String type,int graySize,boolean isSeparate,
									  boolean isCleanImage){
		try {
			saveCheckCodeImg(checkCodeUrltemp,httpClient,type);
			String str = distingusshCode(checkCodePath + type +"checkCode.jpg", 20,20,charsNum,4,type,graySize,isSeparate,isCleanImage);
			return str;
		} catch (Exception ex) {
			ex.printStackTrace();
			//LoggerUtil.error(new ImageProcessUtil().getClass(),"识别验证码失败！",ex);
		}
		return null;
	}
	
	
	
	/**
	 * 从网上爬取图片验证码并且保存到本地
	 *  
	 * @author stan.c
	 * @date 2019年1月22日
	 */
	public static void saveCheckCodeImg(String checkCodeUrltemp,CloseableHttpClient httpClient,String type){
		 CloseableHttpResponse response = null;
		 FileOutputStream os = null;
        HttpPost post = new HttpPost(checkCodeUrltemp);
        try {
            response = httpClient.execute(post);
            InputStream is = response.getEntity().getContent();
            os = new FileOutputStream(new File(checkCodePath + type +"checkCode.jpg"));
            byte[] b = new byte[1024];
            while (is.read(b, 0, b.length) != -1) {
                os.write(b, 0, b.length);
            }
        } catch (Exception ex) {
        	ex.printStackTrace();
           //LoggerUtil.error(new ImageProcessUtil().getClass(), "下载图片验证码失败！",ex);
        }finally{
        	if(os != null){
        		try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	if(response != null){
        		try {
					response.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
	}
	
	
	/**
	 * 识别验证码
	 * @return
	 * @throws IOException 
	 */
	public static  String distingusshCode(String fileUrl,int width,int height,int charsNum,int charWidth,String type,int graySize,boolean isSeparate,
										  boolean isCleanImage) throws IOException{
		List<BufferedImage> list=process(fileUrl, 20,20,charsNum,charWidth,type,graySize,isSeparate,isCleanImage);
		String yanzhengma = "";
		StringBuffer bfStr=new StringBuffer();
		 //2.0识别
        if (list.size() > 0){
            //2.1读取字模
        	List<String> zimo = readTxtFile(zimoFile + type + "ftl.txt");
        	//LoggerUtil.info(new ImageProcessUtil().getClass(), zimoFile);
        	int i=0;
            for(BufferedImage  bf:list){
            	grayDeal(bf,graySize);
            	i++;
            	ImageIO.write(bf, "jpg", new File(checkCodePath + "distingusshCode"+i+".jpg"));
            	bfStr.setLength(0);
                //2.2图片--->特征码
            	getImageCode(bf,bfStr);
                String code =bfStr.toString();
                int rate = 0;
                String subCode = "";
                for(String str:zimo){
                    String[] subZimo = str.split("--");
                    int temp =calcRate(code, subZimo[1]);
                    if (temp > rate){
                        rate = temp;
                        subCode = subZimo[0];
                    }
                }
                yanzhengma += subCode;
            }
        }
        return yanzhengma;
	}
	
	
	/**
	 * 计算相似度
	 * @param t1
	 * @param t2
	 * @return
	 */
	public static  int calcRate(String t1, String t2){
        if (t1.length() > 0 && t2.length() > 0){
            char[] b1 = t1.toCharArray();
            char[] b2 = t2.toCharArray();
            int cnt = 0;
            for (int i = 0; i < b1.length; i++){
                if (b1[i] == b2[i]){
                    cnt++;
                }
            }
            return cnt * 100 / b2.length;
        } else{
        	 return 0;
        }
    }
	
	/**
	 * 处理图片
	 * 1、灰度处理
	 * ２、噪点判断去除
	 * ３、二值化
	 * ４、图片分割
	 * 5、字模制做
	 *
	 * @param fileUrl
	 * @param num
	 * @return
	 * @throws IOException 
	 */
	public static List<BufferedImage>  process(String fileUrl,int width,int height,int charsNum,int charWidth,String type,int graySize,boolean isSeparate,
											   boolean isCleanImage) throws IOException {
		Color newColor = new Color(255, 255, 255);
		File srcFile = new File(fileUrl);
		BufferedImage file = ImageIO.read(srcFile);
		//BufferedImage file=rotateImg(srcImg,25,newColor); 	//旋转25度
		grayDeal(file, graySize);                                //灰度处理
		if (isSeparate) {
			cleanImage(file, file.getHeight(), file.getWidth());//去除干扰线
		}
		if (isCleanImage) {
			separate(file, file.getHeight(), file.getWidth());//分割图片
		}
		removeNoise(file, 1);                                //去掉噪点
		ImageIO.write(file, "jpg", new File(checkCodePath + type + "images" + ".jpg"));
		return cutImage(file, width, height, charsNum, charWidth);
	}

	/**
	 * 取得图片二值码
	 * @param bfImage
	 * @param bfStr
	 */
	public static void getImageCode(BufferedImage bfImage,StringBuffer bfStr){
		int width = bfImage.getWidth();
		int height = bfImage.getHeight();

		int minX = bfImage.getMinX();
		int minY = bfImage.getMinY();

		for (int y = minY; y < height; y++) {
			for (int x = minX; x < width; x++) {
				int gray=getRgb(bfImage,x, y);
				bfStr.append(gray==255?0:1);
				//System.out.print(gray==255?"- ":"* ");
			}
			//System.out.println();
		}
	}
	
	
	/**
     * 功能：Java读取txt文件的内容
     * 步骤：1：先获得文件句柄
     * 2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
     * 3：读取到输入流后，需要读取生成字节流
     * 4：一行一行的输出。readline()。
     * 备注：需要考虑的是异常情况
     * @param filePath
     */
    public static List<String> readTxtFile(String filePath){
        try {
        	List<String> list=new ArrayList<String>();
            String encoding="GBK";
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                	list.add(lineTxt);
                }
                read.close();
                return list;
        }else{
            System.out.println("找不到指定的文件");
        }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
		return null;
    }
	
	 
	
	
	 /**
	  * 确定是四字符会有精细处理
	  * @param img
	  * @param Bitmap
	  * @return
	  */
	public static List<Boundary> getImgBoundaryList(BufferedImage img, int charsNum,int charWidth) {
		List<Boundary> imgList = new ArrayList<Boundary>();
		int startY = 0;
		int endY = 0;
		int startX = 0;
		int endX = 0;
		for (int i = 0; i < charsNum; i++) {
			startX = getStartBoundaryX2(img, startX);
			if (startX >= 0) {
				int tempStartX = startX;
				boolean flag = false;
				while (!flag && tempStartX < img.getWidth()) {
					tempStartX++;
					endX = getEndBoundaryX2(img, tempStartX);
					flag = endX - tempStartX >= charWidth;// 最小高度
				}
				startY = getStartBoundaryY2(img, startY, startX,endX);
				
				endY = getEndBoundaryY2(img, startY+1, startX,endX);
				// 标记
				if (endX > 0 && endX - startX >= 4){// 最小高度
					Boundary bd = new Boundary();
					bd.setEndY(endY);
					bd.setStartY(startY);
					bd.setStartX(startX);
					bd.setEndx(endX);
					imgList.add(bd);
					//System.out.println(startX + "--" + startY + "--" + endX+ "---" + endY);
				}
				startX = endX;
			} else {
				break;
			}
		}
		if (charsNum == 4 && imgList.size() != 4) {
			// list = NotFourChars(img, out _bmp, list,charWidth);
		} else {
			// _bmp = bmp;
		}
		List<Boundary> tempBdList = imgList;
		for (int i = 0; i < tempBdList.size(); i++) {
			if (!((tempBdList.get(i).getEndY() - tempBdList.get(i).getStartY() >= charWidth) && // 最小宽度比较
			(tempBdList.get(i).getEndx() - tempBdList.get(i).getStartX() >= 5))){		// 最小高度
				imgList.remove(tempBdList.get(i));
			}
		}
		return imgList;
	}
	
	
	/**
	 * 字符数目是四个，有切割后不是4个的话会做进一步处理
	 * @param img
	 * @param ww
	 * @param hh
	 * @param is4Chars
	 * @return
	 */
    public static List<BufferedImage> cutImage(BufferedImage img, int width, int height, int  charsNum,int charWidth)
    {
        List<Boundary> list =getImgBoundaryList(img,charsNum,charWidth);
        List<BufferedImage> imgList = new ArrayList<BufferedImage>();
        

        for (int i = 0; i < list.size(); i++){
            Boundary bd =list.get(i);
            int _startX = bd.getStartX();
            int _startY = bd.getStartY(); 
            int _endX = bd.getEndx();
            int _endY = bd.getEndY();
            bd.setStartX(_startX);
            bd.setStartY(_startY);
            bd.setEndx(_endX-_startX);
            bd.setEndY(_endY-_startY);
            try {
				BufferedImage tempBuffer=cut(img,bd);
				imgList.add(zoomImage(tempBuffer,width,height));
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        return imgList;

    }


	/**
	 * 灰度处理
	 * @param srcFileName
	 * @param descFileName
	 * @throws IOException
	 */
    public static void grayDeal(BufferedImage srcImg,Integer size) throws IOException{
		int width = srcImg.getWidth();
		int height = srcImg.getHeight();

		int minX = srcImg.getMinX();
		int minY = srcImg.getMinY();

		for (int y = minY; y < height; y++) {
			for (int x = minX; x < width; x++) {
				int gray=getRgb(srcImg,x, y);
				int grayMax=0;
				if(gray<size){
					grayMax = 0;
				}else{
					grayMax = 255;
				}
				Color newColor = new Color(grayMax,grayMax, grayMax);
				srcImg.setRGB(x, y, newColor.getRGB());
			}
		}
	}
	
	/**
	 * 去除噪点
	 * @param img
	 * @param maxAroundPoints  最大的燥点连点数
	 * @return
	 */
    public static void  removeNoise(BufferedImage image, int maxAroundPoints){
    	int width = image.getWidth();
		int height = image.getHeight();
		for(int y=0;y<height;y++){
			for(int x=0;x<width;x++){
				if(y==0||y==height-1||x==0||x==width-1){  //边界点，直接当作噪点去除掉
					Color newColor = new Color(255,255, 255);
					image.setRGB(x, y, newColor.getRGB());   //设直为空的背景
				}else{
					 int aroundPoint = 0;
					 if( getRgb(image,x-1, y)!=255){//左边
						 aroundPoint++; 
					 }
					 if( getRgb(image,x+1, y)!=255){//右边
						 aroundPoint++; 
					 }
					 if( getRgb(image,x, y-1)!=255){//正上方
						 aroundPoint++; 
					 }
					 if( getRgb(image,x, y+1)!=255){//正下方
						 aroundPoint++; 
					 }
					 if( getRgb(image,x-1, y-1)!=255){//左上角
						 aroundPoint++; 
					 }
					 if( getRgb(image,x+1, y-1)!=255){//右上角
						 aroundPoint++; 
					 }
					 if( getRgb(image,x-1, y+1)!=255){//左下方
						 aroundPoint++; 
					 }
					 if( getRgb(image,x+1, y+1)!=255){//右下方
						 aroundPoint++; 
					 }
					 
					 if (aroundPoint < maxAroundPoints){
						Color newColor = new Color(255,255, 255);
						image.setRGB(x, y, newColor.getRGB());   //设直为空的背景
                     }
					 //System.out.print(getRgb(image,x, y)==0?"   "+getRgb(image,x, y):" "+getRgb(image,x, y));
				}
			}
			 //System.out.println();
		}
//		System.out.println();
//		System.out.println();
//		for(int y=0;y<height;y++){
//			for(int x=0;x<width;x++){
//					 System.out.print(getRgb(image,x, y)==0?"   "+getRgb(image,x, y):" "+getRgb(image,x, y));
//			}
//			 System.out.println();
//		}
    }
    
    /**
     * 返回image
     * @param srcImg
     * @param x
     * @param y
     * @return
     */
    public  static Integer getRgb(BufferedImage srcImg ,Integer x,Integer y){
    	int rgb = srcImg.getRGB(x, y);
		Color color = new Color(rgb); // 根据rgb的int值分别取得r,g,b颜色。

		// 首先灰度化，灰度值=0.3R+0.59G+0.11B：
		int gray = (int) (0.3 * color.getRed() + 0.59 * color.getGreen() + 0.11 * color.getBlue());
		return gray;
    }
    
    /* 
     * 图片旋转 
     */  
   public static BufferedImage  rotateImg(BufferedImage image, int degree,  
           Color bgcolor) throws IOException {  
       int iw = image.getWidth();// 原始图象的宽度  
       int ih = image.getHeight();// 原始图象的高度  
       int w = 0;  
       int h = 0;  
       int x = 0;  
       int y = 0;  
       degree = degree % 360;  
       if (degree < 0)  
           degree = 360 + degree;// 将角度转换到0-360度之间  
       double ang = Math.toRadians(degree);// 将角度转为弧度  
     
       /** 
        * 确定旋转后的图象的高度和宽度 
        */  
     
       if (degree == 180 || degree == 0 || degree == 360) {  
           w = iw;  
           h = ih;  
       } else if (degree == 90 || degree == 270) {  
           w = ih;  
           h = iw;  
       } else {  
           int d = iw + ih;  
           w = (int) (d * Math.abs(Math.cos(ang)));  
           h = (int) (d * Math.abs(Math.sin(ang)));  
       }  
     
       x = (w / 2) - (iw / 2);// 确定原点坐标  
       y = (h / 2) - (ih / 2);  
       BufferedImage rotatedImage = new BufferedImage(w, h, image.getType());  
       Graphics2D gs = (Graphics2D) rotatedImage.getGraphics();  
       if (bgcolor == null) {  
           rotatedImage = gs.getDeviceConfiguration().createCompatibleImage(w,  
                   h, Transparency.OPAQUE);  
       } else {  
           gs.setColor(bgcolor);  
           gs.fillRect(0, 0, w, h);// 以给定颜色绘制旋转后图片的背景  
       }  
       AffineTransform at = new AffineTransform();  
       at.rotate(ang, w / 2, h / 2);// 旋转图象  
       at.translate(x, y);  
       AffineTransformOp op = new AffineTransformOp(at,  AffineTransformOp.TYPE_BICUBIC);  
       op.filter(image, rotatedImage);  
       return rotatedImage;
   } 
    
   
 

   /**
    * Y轴开始的边界
    * @param img
    * @param start
    * @return
    */
   public static  int getStartBoundaryY2(BufferedImage srcImg,int start, int startX, int endX){
	   int startB = 0;
		for (int i = 0; i < srcImg.getHeight(); i++) {
			for (int j = startX; j < endX; j++) {
				// 遍历各个像素，获得bmp位图每个像素的RGB对象
				if (getRgb(srcImg, j, i) != 255) {
					startB = i;
					break;
				} else
					continue;
			}
			if (startB != 0) {
				break;
			} else
				continue;
		}
		return startB - 1;
   }

  
	  /**
	   * Y轴结束的边界
	   * @param img
	   * @param start
	   * @return
	   */
		private static int getEndBoundaryY2(BufferedImage srcImg,int start, int startX, int endX) {
			int endB = 0;
			for (int i =start; i < srcImg.getHeight(); i++) {
				int cnt = 0;
				for (int j = startX; j < endX; j++) {
					// 遍历各个像素，获得bmp位图每个像素的RGB对象
					if (getRgb(srcImg, j, i) == 255) {
						cnt++;
						continue;
					} else
						break;
				}
				if (cnt == endX - startX){// 防止把i的点也算进去
					endB = i;
					//break;
				}
			}
			return endB;
		}
	
	
	 /**
	    *  X轴开始的边界
	    * @param img
	    * @param startY
	    * @param endY
	    * @return
	    */
		private static int getStartBoundaryX2(BufferedImage srcImg, int start) {
			int startB = 0;
			for (int i = start; i < srcImg.getWidth(); i++) {
				for (int j = 0; j <srcImg.getHeight(); j++) {
					// 遍历各个像素，获得bmp位图每个像素的RGB对象
					if (getRgb(srcImg, i, j) != 255) {
						startB = i;
						break;
					}
				}
				if (startB != 0) {
					break;
				}
			}
			if (startB == start) {
				return startB + 2;
			} else {
				return startB - 1;
			}
		}

   
	/**
	 * 获得X轴结束边界
	 * @param img
	 * @param startX
	 * @param startY
	 * @param endY
	 * @return
	 */
	private static int getEndBoundaryX2(BufferedImage srcImg, int start) {
		int endB = 0;
		for (int i = start; i < srcImg.getWidth(); i++) {
			int cnt = 0;
			for (int j = 0; j < srcImg.getHeight(); j++) {
				// TODO：全白
				if (getRgb(srcImg, i, j) == 255) {
					cnt++;
					continue;
				} else
					break;
			}
			if (srcImg.getHeight() == cnt && cnt <= srcImg.getHeight()) {
				endB = i;
				break;
			}
		}
		return endB;
	}
	
	 /** 
     * @param srcpath String 原文件路径 
     * @param subpath String 切割后的存盘路径 
     * @param imageBean ImageBean  
     *  
     * 根据原文件路径，切割后的存盘路径，imageBean ，切割图片 并保存 
     * */  
    public static BufferedImage  cut(BufferedImage image,Boundary imageBean) throws IOException {  
          /*
           * 返回包含所有当前已注册 ImageReader 的 Iterator，这些 ImageReader 声称能够解码指定格式。
           * 参数：formatName - 包含非正式格式名称 .（例如 "jpeg" 或 "tiff"）等 。
           */
          Iterator<ImageReader> it = ImageIO
                  .getImageReadersByFormatName("jpg");
          /**因为是内存中的图片对象，所以没有后缀，就给一个jpg后缀，我给png后缀出错，不知是不是我的BufferedImage对象不对*/
          ImageReader reader = it.next();
          // 获取图片流

          ByteArrayOutputStream os = new ByteArrayOutputStream();  
          ImageIO.write(image, "jpg", os);  
          ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(os.toByteArray()));

          /*
           * <p>iis:读取源.true:只向前搜索 </p>.将它标记为 ‘只向前搜索’。
           * 此设置意味着包含在输入源中的图像将只按顺序读取，可能允许 reader 避免缓存包含与以前已经读取的图像关联的数据的那些输入部分。
           */
          reader.setInput(iis, true);
          /*
           * <p>描述如何对流进行解码的类<p>.用于指定如何在输入时从 Java Image I/O
           * 框架的上下文中的流转换一幅图像或一组图像。用于特定图像格式的插件 将从其 ImageReader 实现的
           * getDefaultReadParam 方法中返回 ImageReadParam 的实例。
           */
          ImageReadParam param = reader.getDefaultReadParam();
          /*
           * 图片裁剪区域。Rectangle 指定了坐标空间中的一个区域，通过 Rectangle 对象
           * 的左上顶点的坐标（x，y）、宽度和高度可以定义这个区域。
           */
         // System.out.println(imageBean.getStartX()+"+++"+imageBean.getStartY()+"+++"+imageBean.getEndx()+"+++"+imageBean.getEndY());
          Rectangle rect = new Rectangle(imageBean.getStartX(),imageBean.getStartY(),imageBean.getEndx(),imageBean.getEndY());
          // 提供一个 BufferedImage，将其用作解码像素数据的目标。
          param.setSourceRegion(rect);
          /*
           * 使用所提供的 ImageReadParam 读取通过索引 imageIndex 指定的对象，并将 它作为一个完整的
           * BufferedImage 返回。
           */
          BufferedImage bi = reader.read(0, param);

          iis.close();
          return bi;
    }  
    
    /** 
     * @param im 
     *            原始图像 
     * @param resizeTimes 
     *            倍数,比如0.5就是缩小一半,0.98等等double类型 
     * @return 返回处理后的图像 
     */  
    public static BufferedImage zoomImage(BufferedImage im,int toWidth,int toHeight) {  
          
        BufferedImage result = null;  
  
        try {  
            /* 新生成结果图片 */  
            result = new BufferedImage(toWidth, toHeight,  BufferedImage.TYPE_INT_RGB);  
  
            result.getGraphics().drawImage(  im.getScaledInstance(toWidth, toHeight,   Image.SCALE_SMOOTH), 0, 0, null);
        } catch (Exception e) {  
            System.out.println("创建缩略图发生异常" + e.getMessage());  
        }  
          
        return result;  
  
    }


	public static void cleanImage(BufferedImage binaryBufferedImage, int h, int w) {
		//去除干扰线条
		for (int y = 1; y < h - 1; y++) {
			for (int x = 1; x < w - 1; x++) {
				boolean flag = false;
				if (isBlack(binaryBufferedImage.getRGB(x, y))) {
					//左右均为空时，去掉此点
					if (isWhite(binaryBufferedImage.getRGB(x - 1, y)) && isWhite(binaryBufferedImage.getRGB(x + 1, y))) {
						flag = true;
					}
					//上下均为空时，去掉此点
					if (isWhite(binaryBufferedImage.getRGB(x, y + 1)) && isWhite(binaryBufferedImage.getRGB(x, y - 1))) {
						flag = true;
					}
					//斜上下为空时，去掉此点
					if (isWhite(binaryBufferedImage.getRGB(x - 1, y + 1)) && isWhite(binaryBufferedImage.getRGB(x + 1, y - 1))) {
						flag = true;
					}
					if (isWhite(binaryBufferedImage.getRGB(x + 1, y + 1)) && isWhite(binaryBufferedImage.getRGB(x - 1, y - 1))) {
						flag = true;
					}
					if (flag) {
						binaryBufferedImage.setRGB(x, y, -1);
					}
				}
			}
		}
	}


	public static boolean isBlack(int colorInt)
	{
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() <= 300)
		{
			return true;
		}
		return false;
	}

	public static boolean isWhite(int colorInt)
	{
		Color color = new Color(colorInt);
		if (color.getRed() + color.getGreen() + color.getBlue() > 300)
		{
			return true;
		}
		return false;
	}

	public static int isBlackOrWhite(int colorInt)
	{
		if (getColorBright(colorInt) < 30 || getColorBright(colorInt) > 730)
		{
			return 1;
		}
		return 0;
	}

	public static int getColorBright(int colorInt)
	{
		Color color = new Color(colorInt);
		return color.getRed() + color.getGreen() + color.getBlue();
	}

	public static void separate(BufferedImage bufferedImage, int h, int w){
		for(int i = 0;i < h;i++){
			for(int j = 0;j < w;j++){
				if(j == 21 || j== 22 || j == 37 || j == 38 || j == 50 || j == 51){
					Color newColor = new Color(255, 255, 255);
					bufferedImage.setRGB(j,i,newColor.getRGB());
				}
			}
		}
	}
      
}
