/*
 * Copyright 2015 Evgeny Dolganov (evgenij.dolganov@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package och.util.string;

import static java.lang.Character.*;
import static java.util.Collections.*;
import static och.util.ArrayUtil.*;
import static och.util.FileUtil.*;
import static och.util.StreamUtil.*;
import static och.util.StringUtil.*;
import static och.util.Util.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Счетчик слов.
 * Для хранения промежуточных результатов использует
 * оперативную память и, если ее недостаточно, файловую систему.
 * 
 * <p>
 * Кодировка входящего потока задается системной переменной -Dfile.encoding
 * 
 * <p>
 * Если для работы счетчика требуется мало RAM, можно запустить его с флагом 'smallram',
 * или же программно задать RamTopSize (размер списка уникальных слов в оперативной памяти).
 */
public class WordsCounter {
	


	public static void main(String[] args) throws IOException {
		
		//нужно ли использовать мало оперативной памяти
		boolean useSmallRam = args.length > 0 && "smallram".equals(args[0]);
		
		WordsCounter counter = new WordsCounter();
		counter.setRamTopSize(useSmallRam? 10000 : DEFAULT_RAM_TOP_SIZE);
		
		//подсчет слов
		int topSize = 10;
		int wordMaxSize = DEFAULT_WORD_MAX_SIZE;
		List<WordStat> result = counter.getTop(System.in, topSize, wordMaxSize);
		
		//вывод результата
		for (WordStat stat : result) {
			System.out.println(stat.word + " " + stat.count);
		}
	}
	
	
	
	/** Размер списка уникальных слов в оперативной памяти по умолчанию */
	public static final int DEFAULT_RAM_TOP_SIZE = 1000000;
	
	/** Максимальная длина слова по умолчанию */
	public static final int DEFAULT_WORD_MAX_SIZE = 30;
	
	/** Результат работы */
	public static class WordStat implements Comparable<WordStat>{
		
		/** найденное слово */
		public final String word;
		
		/** количество вхождений */
		private long count;
		
		public WordStat(String word, long count) {
			this.word = word;
			this.count = count > 0? count : 0;
		}
		
		/** увеличить счетчик слова на 1 */
		public void incCount(){
			if(count == Long.MAX_VALUE) return;
			count++;
		}
		
		/** уменьшить счетчик слова на 1 */
		public void decCount(){
			if(count == 0) return;
			count--;
		}
		
		public long count(){
			return count;
		}
		
		@Override
		public int compareTo(WordStat o) {
			//count - desc
			//word - asc
			int result = Long.compare(o.count, count);
			return result != 0? result : word.compareTo(o.word);
		}

		@Override
		public String toString() {
			return "WordStat [word=" + word + ", count=" + count + "]";
		}
	}

	
	
	private boolean debug = false;
	private File parentDir = new File(".");
	private boolean removeIndexesFiles = true;
	private int ramTopSize = DEFAULT_RAM_TOP_SIZE;
	
	/**
	 * Размер списка уникальных слов в оперативной памяти.
	 * Если все слова умещяются в списке, то подсчет происходит быстро 
	 * (т.к. полностью идет в RAM). Иначе промежуточные данные 
	 * сохраняются в файловой системе. Это замедляет подсчет слов, но
	 * позволяет экономить RAM. 
	 */
	public void setRamTopSize(int ramTopSize) {
		this.ramTopSize = ramTopSize;
	}
	
	/** Вывод доп. информации в логи. По умолчанию - нет */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	/** 
	 * Директория, в которой можно создать индексный 
	 * файл при нехватке оперативной памяти. По умолчанию - текущая директория. 
	 */
	public void setTmpIndexDirParent(File parentDir) {
		this.parentDir = parentDir;
	}
	
	/** Нужно ли удалять созданный файл после окончания работы */
	public void setRemoveIndexesFiles(boolean removeIndexesFiles) {
		this.removeIndexesFiles = removeIndexesFiles;
	}

	
	/**
	 * Подсчет слов из потока
	 */
	public List<WordStat> getTop(InputStream in, int topMaxSize) throws IOException {
		return getTop(in, topMaxSize, DEFAULT_WORD_MAX_SIZE);
	}
	
	/**
	 * Подсчет слов из строки
	 */
	public List<WordStat> getTop(String words, int topMaxSize) throws IOException {
		return getTop(new ByteArrayInputStream(getBytesUTF8(words)), topMaxSize, DEFAULT_WORD_MAX_SIZE);
	}
	
	public List<WordStat> getTop(String words, int topMaxSize, int wordMaxSize) throws IOException {
		return getTop(new ByteArrayInputStream(getBytesUTF8(words)), topMaxSize, wordMaxSize);
	}
	
	/**
	 * Подсчет слов из потока
	 * @param in - входной поток
	 * @param topMaxSize - макс.размер результата
	 * @param wordMaxSize - макс.размер слова (слова большего размера будут игнорироваться)
	 * @return список топ слов
	 * @throws IOException ошибка при работе с файловой системой
	 */
	public List<WordStat> getTop(InputStream in, int topMaxSize, int wordMaxSize) throws IOException {
		
		if(in == null) return emptyList();
		if(topMaxSize < 1) return emptyList();
		if(wordMaxSize < 1) return emptyList();
		
		WordsIndex index = new WordsIndex(parentDir, ramTopSize, topMaxSize, wordMaxSize);
		index.setDebug(debug);
		
		BufferedReader reader = null;
		try {
			
			reader = new BufferedReader(new InputStreamReader(in));
			
			//Обход потока
			StringBuilder sb = new StringBuilder();
			int ch;
			while ((ch = reader.read()) != -1) {
				ch = toLowerCase(ch);
				//слово закончилось
				if(isSeparator(ch)){
					String word = sb.toString();
					sb = new StringBuilder();
					if( word.length() > 0){
						//добавляем слово в индекс
						index.add(word);
					}
				}
				//в середине слова
				else {
					sb.append((char)ch);
				}
			}
			String lastWord = sb.toString();
			if( lastWord.length() > 0) {
				index.add(lastWord);
			}
			
			return index.getTop();
		} finally {
			close(reader);
			if(removeIndexesFiles) index.clean();
		}
		

	}
	
	/** Является ли символ разделителем */
	public static boolean isSeparator(int ch) {
		if(ch == ' ') return true;
		if(ch == '\n') return true;
		if(ch == '\t') return true;
		if(ch == '\r') return true;
		if(ch == '\0') return true;
		//if(Character.isSpaceChar(ch)) return true;
		//if(Character.isWhitespace(ch)) return true;
		return false;
	}
	
	
	
	/** 
	 * Хранилище индексов слов
	 */
	public static class WordsIndex {
		
		private int ramTopSize;
		private int topMaxSize;
		private int wordMaxSize;
		private ArrayList<WordStat> top;
		private HashMap<String, WordStat> topByWord;
		private boolean isDebug;
		
		private IndexesStorage storage;
		private boolean sorted = true;

		public WordsIndex(File parentDir, int ramTopSize, int topMaxSize, int wordMaxSize) throws IOException {
			
			this.topMaxSize = topMaxSize;
			this.wordMaxSize = wordMaxSize;
			
			if(ramTopSize < topMaxSize) ramTopSize = topMaxSize;
			this.ramTopSize = ramTopSize;
			
			top = new ArrayList<WordsCounter.WordStat>(ramTopSize);
			topByWord = new HashMap<String, WordsCounter.WordStat>(ramTopSize);
			
			storage = new IndexesStorage(parentDir, wordMaxSize);
		}

		public void setDebug(boolean isDebug) {
			this.isDebug = isDebug;
		}


		/** Добавить слово в индекс */
		public void add(String word) throws IOException {
			
			if(word == null) return;
			if(word.length() > wordMaxSize) {
				if(isDebug) System.out.println("skip too long word: " + (word.length() < 1000? word : "[long word]"));
				return;
			}
			if(storage.isInvaild(word)) {
				if(isDebug) System.out.println("skip invalid for storage word: " + (word.length() < 1000? word : "[long word]"));
				return;
			}
			
			//Слово есть в топе RAM
			WordStat inTop = topByWord.get(word);
			if(inTop != null){
				inTop.incCount();
				sorted = false;
				return;
			}
			
			//Слова нет в топе
			WordStat stat = null;
			if(top.size() < ramTopSize) {
				stat = new WordStat(word, 0);
			}
			else {
				//если топ полный - ищем слово в файловом хранилище
				stat = storage.getFromStorage(word);
				if(stat == null) {
					stat = new WordStat(word, 0);
				}
			}

			stat.incCount();
			
			//Топ еще не заполнен - просто добавляем в топ
			if(top.size() < ramTopSize){
				top.add(stat);
				sorted = false;
				topByWord.put(word, stat);
				return;
			}
			

			//Топ заполнен
			//Берем последнее слово из топа
			if(!sorted){
				sort(top);
				sorted = true;
			}
			int lastTopIndex = ramTopSize-1;
			WordStat lastInTop = top.get(lastTopIndex);
			
			//Слово меньше чем все из топа
			if(lastInTop.compareTo(stat) < 0){
				storage.putToStorage(stat);
				return;
			}

			
			//Добавляем новое слово в топ
			//-- удаляем последнее
			top.remove(lastTopIndex);
			topByWord.remove(lastInTop.word);
			storage.putToStorage(lastInTop);
			
			//-- добавляем новое
			top.add(stat);
			sorted = false;
			topByWord.put(word, stat);
			
		}
		
		/**
		 * Получить текущее состояние топа
		 */
		public List<WordStat> getTop() {
			if(!sorted) {
				sort(top);
				sorted = true;
			}
			return top.size() > topMaxSize? top.subList(0, topMaxSize) : top;
		}
		
		/** Очистить файловую систему (если она использовалась) */
		public void clean() {
			storage.remove();
		}
		
		
	}
	
	
	
	/**
	 * Файловое хранилище промежуточных результатов.
	 * Используется при нехватке RAM.
	 * 
	 * Реализация напоминает HashMap:
	 * - у слова берется hash, который является индексом к нужному смещению в файле
	 * - по данному смещению считывается, либо записывается информация о словах с данным hash
	 */
	public static class IndexesStorage {
		
		public static final int DEFAULT_HASH_LIMIT = 1000;
		public static final int DEFAULT_PAGE_ELEM_COUNT = 10000;
		public static final int HASH_RECORD_SIZE = 8 + 4;
		

		private File parentDir;
		private int hashLimit;
		private int pageElemCount;
		
		private File root;
		private byte[] hashesInfo;
		private File pagesFile;
		private RandomAccessFile pagesRaf;
		private int wordBytesSize;
		private int recordSize;
		private int pageSize;
		
		public IndexesStorage(File parentDir, int wordMaxSize) throws IOException {
			this(parentDir, wordMaxSize, DEFAULT_HASH_LIMIT, DEFAULT_PAGE_ELEM_COUNT);
		}

		public IndexesStorage(File parentDir, int wordMaxSize, int hashLimit, int pageElemCount) throws IOException {
			
			this.parentDir = parentDir;
			this.hashLimit = hashLimit;
			this.pageElemCount = pageElemCount;
			
			wordBytesSize = wordMaxSize*2;
			recordSize = wordBytesSize+1+8+1; //wordBytes+' '+count+'\n\'
			pageSize = recordSize*pageElemCount;
			
		}
		
		/** создание файлов */
		private void initFiles() throws IOException{
			
			if(root != null) return;
			
			hashesInfo = new byte[hashLimit*HASH_RECORD_SIZE];
			
			root = new File(parentDir, "/index-tmp-"+randomSimpleId());
			root.mkdirs();
			
			pagesFile = new File(root, "pages.data");
			writeFileUTF8(pagesFile, "/* pages by hash */\n");
			pagesRaf = new RandomAccessFile(pagesFile, "rw");
			
		}
		
		/** подходит ли размер слова под размер выделенного для него блока в файле */
		public boolean isInvaild(String word) {
			byte[] bytes = getBytesUTF8(word);
			return bytes.length > wordBytesSize;
		}
		
		/** удаляем созданные файлы */
		public void remove() {
			if(root != null){
				close(pagesRaf);
				deleteDirRecursive(root);
			}
		}

		/** добавление/обновление статистики в файл */
		public void putToStorage(WordStat stat)throws IOException {
			
			initFiles();

			int hashOffset = getHashOffset(stat.word);
			
			long pageOffset = getLong(hashesInfo, hashOffset);
			int recordCount = getInt(hashesInfo, hashOffset+8);
			
			byte[] recordBytes = toRecordBytes(stat);
			
			//слов с подобным хешом, еще не было
			if(recordCount == 0){

				byte[] page = new byte[pageSize];
				copyFromSmallToBig(recordBytes, page, 0);
				
				//запись
				pageOffset = pagesFile.length();
				pagesRaf.seek(pageOffset);
				pagesRaf.write(page);
				recordCount = 1;
				
				//обновление таблицы индексов
				writeLongToArray(pageOffset, hashesInfo, hashOffset);
				writeIntToArray(recordCount, hashesInfo, hashOffset+8);
				
				return;
			}
			

			//ищем слово среди всех уже добавленных по данному хешу
			FindResult findResult = findWordStatInPage(pageOffset, recordCount, stat.word);
			//новое - добавляем в список
			if(findResult.stat == null){
				if(recordCount == pageElemCount) {
					throw new RecordPageIsFullException();
				}
				copyFromSmallToBig(recordBytes, findResult.pageBytes, recordCount*recordSize);
				recordCount++;
				
				//запись
				pagesRaf.seek(pageOffset);
				pagesRaf.write(findResult.pageBytes);
				
				//обновление таблицы индексов
				writeIntToArray(recordCount, hashesInfo, hashOffset+8);
				return;
			}
			
			//уже есть в файле - обновляем
			copyFromSmallToBig(recordBytes, findResult.pageBytes, findResult.recordOffset);
			//запись
			pagesRaf.seek(pageOffset);
			pagesRaf.write(findResult.pageBytes);
			
		}

		/** поиск статистики в файле */
		public WordStat getFromStorage(String word)throws IOException {
			
			initFiles();
			
			int hashOffset = getHashOffset(word);
			
			long pageOffset = getLong(hashesInfo, hashOffset);
			int recordCount = getInt(hashesInfo, hashOffset+8);
			
			if(recordCount == 0){
				return null;
			}
			
			FindResult findResult = findWordStatInPage(pageOffset, recordCount, word);
			return findResult == null? null : findResult.stat;
		}
		
		private static class FindResult {
			public WordStat stat;
			public byte[] pageBytes;
			public int recordOffset;
		}
		
		private FindResult findWordStatInPage(long pageOffset, int recordCount, String word) throws IOException{
			
			FindResult out = new FindResult();
			
			//читаем весь список слов с данным хешем из файла
			out.pageBytes = new byte[pageSize];
			pagesRaf.seek(pageOffset);
			pagesRaf.read(out.pageBytes);
			
			//ищем нужное слово
			int recordOffset;
			byte[] recordBytes = new byte[recordSize];
			for (int i = 0; i < recordCount; i++) {
				recordOffset = i * recordSize;
				arrayCopy(out.pageBytes, recordOffset, recordBytes, 0, recordSize);
				WordStat stat = fromRecordBytes(recordBytes);
				if(stat != null){
					if(stat.word.equals(word)){
						out.stat = stat;
						out.recordOffset = recordOffset;
						break;
					}
				}
			}
			
			return out;
		}
		
		/** расчет хеша слова (смещения в таблице индексов) */
		public int getHashOffset(String word){
			return getHashOffset(word, hashLimit) * HASH_RECORD_SIZE;
		}

		public static int getHashOffset(String word, int hashLimit){
			int out = word.hashCode();
			out = Math.abs(out) % hashLimit;
			return out;
		}
		
		/** запись статистики в байтовый массив */
		public byte[] toRecordBytes(WordStat stat){
			byte[] out = new byte[recordSize];
			byte[] str = getBytesUTF8(stat.word);
			copyFromSmallToBig(str, out, 0);
			out[str.length] = ' ';
			writeLongToArray(stat.count, out, str.length+1);
			out[str.length+1+8] = '\n';
			return out;
		}
		
		/** чтение статистики из байтового массива */
		public WordStat fromRecordBytes(byte[] record){
			
			int endIndex = 0;
			for (int i = record.length-1; i > -1 ; i--){
				if(record[i] == '\n'){
					endIndex = i;
					break;
				}
			}
			
			if(endIndex == 0) return null;
			
			String word = getStr(record, 0, endIndex-9);
			long count = getLong(record, endIndex-8);
			return new WordStat(word, count);
		}
		
		
	}
	

	/** 
	 * Ошибка переполнения списка слов с одинаковым хешем.
	 * При получении такой ошибки, нужно задать большую длину hashLimit:
	 * это увеличит число разлчиных вариантов хешей, но и увеличит размер файла.
	 */
	public static class RecordPageIsFullException extends IOException {
		
		private static final long serialVersionUID = 1L;
		
	}

	


}
