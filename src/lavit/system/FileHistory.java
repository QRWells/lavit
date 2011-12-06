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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

	private int _limit = DEFAULT_LIMIT;
	private LinkedList<File> _files = new LinkedList<File>();

	/**
	 * <p>���Υե��������򥪥֥������Ȥ����ꤵ��Ƥ���ե�����ꥹ�Ȥ����ǿ��ξ���ͤ�������ޤ���</p>
	 * @return ���ꤵ��Ƥ�������
	 */
	public int getLimit()
	{
		return _limit;
	}

	/**
	 * <p>���Υե��������򥪥֥������Ȥ��ޤ�ե�����ꥹ�Ȥ����ǿ��ξ���ͤ����ꤷ�ޤ���</p>
	 * @param limit ���ꤹ������
	 */
	public void setLimit(int limit)
	{
		_limit = limit;
		trimSize();
	}

	/**
	 * <p>�ե���������򤹤٤ƺ�����ޤ���</p>
	 */
	public void clear()
	{
		_files.clear();
	}

	/**
	 * <p>�ե������������ɲä��ޤ���</p>
	 * <p>���Υե����뤬���˴ޤޤ���硢�������Ǥ���Ƭ�ذ�ư���ޤ���</p>
	 * @param file �ɲä���ե�����
	 */
	public void add(File file)
	{
		int index = _files.indexOf(file);

		if (index != -1)
		{
			_files.remove(index);
		}

		_files.addFirst(file);
		trimSize();
	}

	/**
	 * <p>�ե���������Υꥹ�Ȥ��ɤ߼�����ѤȤ��Ƽ������ޤ���</p>
	 * @return �ե���������Υꥹ��
	 */
	public List<File> getFiles()
	{
		return Collections.unmodifiableList(_files);
	}

	/**
	 * <p>�ե���������Υꥹ�Ȥ�ե��������¸���ޤ���</p>
	 * @param fileName ��¸����ե�����̾
	 */
	public void save(String fileName)
	{
		try
		{
			PrintWriter writer = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(fileName))));

			for (File file : _files)
			{
				writer.println(file.getAbsolutePath());
			}
			writer.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * <p>�ե����뤫��ե���������Υꥹ�Ȥ��ɤ߹��ߤޤ���</p>
	 * @param fileName �ɤ߹���ե�����̾
	 * @return �ե��������򥪥֥�������
	 */
	public static FileHistory fromFile(String fileName)
	{
		File file = new File(fileName);

		FileHistory history = new FileHistory();

		if (file.exists())
		{
			try
			{
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(file)));

				String line;
				try
				{
					while ((line = reader.readLine()) != null)
					{
						history.add(new File(line));
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
		return history;
	}

	/**
	 * <p>�ꥹ�Ȥ����ǿ������ꤵ�줿����Ͱʲ����ڤ�ͤ�ޤ���</p>
	 */
	private void trimSize()
	{
		if (_files.size() > _limit)
		{
			if (_files.size() == _limit + 1)
			{
				_files.remove(_files.size() - 1);
			}
			else
			{
				_files = (LinkedList<File>) _files.subList(0, _limit - 1);
			}
		}
	}
}
