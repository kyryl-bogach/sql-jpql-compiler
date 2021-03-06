/*
 * This file is part of Zql.
 *
 * Zql is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Zql is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Zql.  If not, see <http://www.gnu.org/licenses/>.
 */

package demo.data;

import java.io.ByteArrayInputStream;

import org.gibello.zql.ParseException;
import org.gibello.zql.ZStatement;
import org.gibello.zql.ZqlParser;

public class StringDemo {

	public static void main(String args[]) {
		try {

			ZqlParser p = new ZqlParser();

			p.initParser(new ByteArrayInputStream(args[0].getBytes()));
			ZStatement st = p.readStatement();
			System.out.println(st.toString()); // Display the statement

		} catch (ParseException e) {
			System.err.println("PARSE EXCEPTION:");
			e.printStackTrace(System.err);
		} catch (Error e) {
			System.err.println("ERROR");
		} catch (Exception e) {
			System.err.println("CLASS" + e.getClass());
			e.printStackTrace(System.err);
		}
	}

};
