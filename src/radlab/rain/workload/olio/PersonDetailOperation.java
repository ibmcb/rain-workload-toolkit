/*
 * Copyright (c) 2010, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  * Neither the name of the University of California, Berkeley
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Author: Original authors
 * Author: Marco Guazzone (marco.guazzone@gmail.com), 2013
 */

package radlab.rain.workload.olio;


import java.io.IOException;
import java.util.Set;
import radlab.rain.IScoreboard;
import radlab.rain.workload.olio.model.OlioPerson;


/**
 * The PersonDetailOperation is an operation that shows the details of a
 * randomly selected user. The user must be logged in to see the details.
 * <br/>
 * NOTE: Code based on {@code org.apache.olio.workload.driver.UIDriver} class
 * and adapted for RAIN.
 *
 * @author Original authors
 * @author <a href="mailto:marco.guazzone@gmail.com">Marco Guazzone</a>
 */
public class PersonDetailOperation extends OlioOperation 
{
	public PersonDetailOperation(boolean interactive, IScoreboard scoreboard) 
	{
		super(interactive, scoreboard);
		this._operationName = OlioGenerator.PERSON_DETAIL_OP_NAME;
		this._operationIndex = OlioGenerator.PERSON_DETAIL_OP;
	}

	@Override
	public void execute() throws Throwable
	{
		///XXX
		//OlioPerson loggedPerson = this.getUtility().getPerson(this.getSessionState().getLoggedPersonId());
		//if (!this.getUtility().isRegisteredPerson(loggedPerson))
		//{
		//	this.getLogger().warning("No valid user has been found to log-in. Operation interrupted.");
		//	this.setFailed(true);
		//	return;
		//}

		OlioPerson person = this.getUtility().generatePerson();

		String personUrl = null;
		switch (this.getConfiguration().getIncarnation())
		{
			case OlioConfiguration.JAVA_INCARNATION:
				personUrl = this.getGenerator().getPersonDetailURL() + person.userName;
				break;
			case OlioConfiguration.PHP_INCARNATION:
				personUrl = this.getGenerator().getPersonDetailURL() + person.userName;
				break;
			case OlioConfiguration.RAILS_INCARNATION:
				personUrl = this.getGenerator().getPersonDetailURL() + person.id;
				break;
		}

		StringBuilder response = this.getHttpTransport().fetchUrl(personUrl);
		this.trace(personUrl);
		if (!this.getGenerator().checkHttpResponse(response.toString()))
		{
			this.getLogger().severe("Problems in performing request to URL: " + personUrl + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + "). Server response: " + response);
			throw new IOException("Problems in performing request to URL: " + personUrl + " (HTTP status code: " + this.getHttpTransport().getStatusCode() + ")");
		}

		this.loadStatics(this.getGenerator().getPersonStatics());
		this.trace(this.getGenerator().getPersonStatics());

		Set<String> imageUrls = this.parseImages(response.toString());
		this.loadImages(imageUrls);
		this.trace(imageUrls);

		// Save session state
		this.getSessionState().setLastResponse(response.toString());

		this.setFailed(false);
	}
}
