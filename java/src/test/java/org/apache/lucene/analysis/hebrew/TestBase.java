/**
 * ************************************************************************
 * Copyright (C) 2010-2015 by                                            *
 * Itamar Syn-Hershko <itamar at code972 dot com>                     *
 * *
 * This program is free software; you can redistribute it and/or modify  *
 * it under the terms of the GNU Affero General Public License           *
 * version 3, as published by the Free Software Foundation.              *
 * *
 * This program is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 * GNU Affero General Public License for more details.                   *
 * *
 * You should have received a copy of the GNU Affero General Public      *
 * License along with this program; if not, see                          *
 * <http://www.gnu.org/licenses/>.                                       *
 * ************************************************************************
 */
package org.apache.lucene.analysis.hebrew;

import com.code972.hebmorph.DictionaryLoader;
import com.code972.hebmorph.datastructures.DictHebMorph;
import com.code972.hebmorph.hspell.HSpellDictionaryLoader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.hebrew.TokenFilters.AddSuffixTokenFilter;
import org.apache.lucene.analysis.hebrew.TokenFilters.HebrewLemmatizerTokenFilter;
import org.apache.lucene.analysis.hebrew.TokenFilters.MarkHebrewTokensFilter;
import org.apache.lucene.analysis.hebrew.TokenFilters.NiqqudFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.junit.AfterClass;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public abstract class TestBase {
    private static DictHebMorph dict;

    protected synchronized DictHebMorph getDictionary() throws IOException {
        if (dict == null) {
            dict = new HSpellDictionaryLoader().loadDictionaryFromPath(com.code972.hebmorph.TestBase.DICT_PATH);
        }
        return dict;
    }

    protected static File[] getTestFiles() throws IOException {
        List<String> lookedAt = new ArrayList<>();
        for (String s : new String[]{".", "..", "../.."}) {
            File f = new File(s + "/test-files");
            if (f.exists()) return f.listFiles();
            lookedAt.add(f.getCanonicalPath());
        }
        throw new IOException("Cannot find test data, looked at " + lookedAt);
    }

    @AfterClass
    public static void cleanupDictionary() {
        if (dict != null) {
            dict.clear();
            dict = null;
        }
    }


    public static HebrewIndexingAnalyzer getHebrewIndexingAnalyzer() throws IOException {
        if (dict == null) {
            dict = (new HSpellDictionaryLoader().loadDictionaryFromPath(com.code972.hebmorph.TestBase.DICT_PATH));
        }
        return new HebrewIndexingAnalyzer(dict);
    }

    public static HebrewAnalyzer getHebrewIndexingAnalyzerWithStandardTokenizer() throws IOException {
        if (dict == null) {
            dict = (new HSpellDictionaryLoader().loadDictionaryFromPath(com.code972.hebmorph.TestBase.DICT_PATH));
        }
        return new HebrewAnalyzer(dict) {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                Tokenizer src = new StandardTokenizer();
                TokenStream tok = new NiqqudFilter(src);
                tok = new ASCIIFoldingFilter(tok);
                tok = new LowerCaseFilter(tok);
                tok = new MarkHebrewTokensFilter(tok);
                tok = new HebrewLemmatizerTokenFilter(tok, dict);
                tok = new AddSuffixTokenFilter(tok, '$');
                return new TokenStreamComponents(src, tok);
            }
        };
    }

    public static HebrewQueryAnalyzer getHebrewQueryAnalyzer() throws IOException {
        if (dict == null) {
            dict = (new HSpellDictionaryLoader().loadDictionaryFromPath(com.code972.hebmorph.TestBase.DICT_PATH));
        }
        return new HebrewQueryAnalyzer(dict);
    }

    public static HebrewQueryLightAnalyzer getHebrewQueryLightAnalyzer() throws IOException {
        if (dict == null) {
            dict = (new HSpellDictionaryLoader().loadDictionaryFromPath(com.code972.hebmorph.TestBase.DICT_PATH));
        }
        return new HebrewQueryLightAnalyzer(dict);
    }

    public static HebrewExactAnalyzer getHebrewExactAnalyzer() throws IOException {
        if (dict == null) {
            dict = (new HSpellDictionaryLoader().loadDictionaryFromPath(com.code972.hebmorph.TestBase.DICT_PATH));
        }
        return new HebrewExactAnalyzer(dict);
    }

    protected static String readFileToString(String path) throws IOException {
        try (FileInputStream stream = new FileInputStream(new File(path))) {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            return Charset.forName("UTF-8").decode(bb).toString();
        }
    }
}
