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
* @author Felicitas Loeffler
*
*/
public class Competence {
	int rank;
	String competence;
	String rating;
	
	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public Competence(String competence){
		this.competence=competence;
	}
	
	public Competence(int rank,String competence, String rating){
		this.rank=rank;
		this.competence=competence;
		this.rating = rating;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getCompetence() {
		return competence;
	}

	public void setCompetence(String competence) {
		this.competence = competence;
	}
}
