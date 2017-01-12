package info.semanticsoftware.scholarlens;
/**
 * ScholarLens - http://www.semanticsoftware.info/scholarlens
 *
 * This file is part of the ScholarLens component.
 *
 * Copyright (c) 2016, 2017, Semantic Software Lab, http://www.semanticsoftware.info,
 * Friedrich Schiller University Jena, http://fusion.cs.uni-jena.de
 *    Rene Witte
 *    Bahar Sateli
 *    Felicitas Loeffler
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 **/

/**
 * Competency POJO class.
 *
 * @author Rene Witte
 * @author Bahar Sateli
 *
 */
public class Competency {
	private String label = null;
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	private String comment = null;
	/** The original rank of the competence in KB. */
	private int rank = 0;
	
	public Competency(final String lbl, final String cmt, final int rank){
		this.label = lbl;
		this.comment = cmt;
		this.rank = rank;
	}
}
