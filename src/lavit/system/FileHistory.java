package lavit.system;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lavit.Env;

/**
 * <p>�������ե�����������������ޤ���</p>
 * @author Yuuki SHINOBU
 */
public class FileHistory
{
	/**
	 * <p>�ե�����������ݻ�������ξ���ͤδ����ͤǤ���</p>
	 */
	public static final int DEFAULT_LIMIT = 8;

	private static final String HISTORY_FILE = "recentfiles";
	private static FileHistory instance;

	private int limit = DEFAULT_LIMIT;
	private LinkedList<File> files = new LinkedList<File>();

	/**
	 * <p>���Υե��������򥪥֥������Ȥ����ꤵ��Ƥ���ե�����ꥹ�Ȥ����ǿ��ξ���ͤ�������ޤ���</p>
	 * @return ���ꤵ��Ƥ�������
	 */
	public int getLimit()
	{
		return limit;
	}

	/**
	 * <p>���Υե��������򥪥֥������Ȥ��ޤ�ե�����ꥹ�Ȥ����ǿ��ξ���ͤ����ꤷ�ޤ���</p>
	 * @param limit ���ꤹ������
	 */
	public void setLimit(int limit)
	{
		this.limit = limit;
		trimSize();
	}

	/**
	 * <p>�ե���������򤹤٤ƺ�����ޤ���</p>
	 */
	public void clear()
	{
		files.clear();
	}

	/**
	 * <p>�ե������������ɲä��ޤ���</p>
	 * <p>���Υե����뤬���˴ޤޤ���硢�������Ǥ���Ƭ�ذ�ư���ޤ���</p>
	 * @param file �ɲä���ե�����
	 */
	public void add(File file)
	{
		try
		{
			file = file.getCanonicalFile();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		int index = files.indexOf(file);

		if (index != -1)
		{
			files.remove(index);
		}

		files.addFirst(file);
		trimSize();
	}

	/**
	 * <p>�ե���������Υꥹ�Ȥ��ɤ߼�����ѤȤ��Ƽ������ޤ���</p>
	 * @return �ե���������Υꥹ��
	 */
	public List<File> getFiles()
	{
		return Collections.unmodifiableList(files);
	}

	/**
	 * <p>�ե���������Υꥹ�Ȥ�ե��������¸���ޤ���</p>
	 */
	private void save()
	{
		File historyFile = Env.getPropertyFile(HISTORY_FILE);
		String charset = "UTF-8";
		try
		{
			PrintWriter writer = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(historyFile), charset)));
			for (File file : files)
			{
				writer.println(file.getAbsolutePath());
			}
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * <p>�ե����뤫��ե���������Υꥹ�Ȥ��ɤ߹��ߤޤ���</p>
	 * @return �ե��������򥪥֥�������
	 */
	public static FileHistory get()
	{
		if (instance != null)
		{
			return instance;
		}

		instance = new FileHistory();
		File historyFile = Env.getPropertyFile(HISTORY_FILE);
		if (historyFile.exists())
		{
			String charset = "UTF-8";
			try
			{
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(historyFile), charset));
				try
				{
					String line;
					while ((line = reader.readLine()) != null)
					{
						instance.add(new File(line));
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				finally
				{
					reader.close();
				}
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				instance.save();
			}
		});
		return instance;
	}

	/**
	 * <p>�ꥹ�Ȥ����ǿ������ꤵ�줿����Ͱʲ����ڤ�ͤ�ޤ���</p>
	 */
	private void trimSize()
	{
		if (files.size() > limit)
		{
			if (files.size() == limit + 1)
			{
				files.remove(files.size() - 1);
			}
			else
			{
				files = (LinkedList<File>)files.subList(0, limit - 1);
			}
		}
	}
}
