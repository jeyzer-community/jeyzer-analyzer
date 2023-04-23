package org.jeyzer.analyzer.output.jmusic;

/*-
 * ---------------------------LICENSE_START---------------------------
 * Jeyzer Analyzer
 * --
 * Copyright (C) 2020 Jeyzer
 * --
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * ----------------------------LICENSE_END----------------------------
 */






/** 
 * The possible bar selections for Mozart's Dice Game
 * Used by the MozartDiceGame class
 * @author Andrew Troedson
 */
 
import jm.music.data.*;
import jm.JMC;

public class MozartPhraseList implements JMC {
	
		//Phrase array which stores all the phrases
		private CPhrase[][] barArray = new CPhrase[8][11];
		CPhrase[][] barArray2 = new CPhrase[8][11];
		
		//the phrase tables and individual phrases are initialized upon the creation of a new instance
		public MozartPhraseList() {
			
			//bar 1
	 		int[] pitchArray1a = {F2,F4};
			CPhrase bar1 = new CPhrase(0.0);
			bar1.addChord(pitchArray1a, C);
			
			int[] pitchArray1b = {D2,D4};
			bar1.addChord(pitchArray1b, C);

			int[] pitchArray1c = {G2,G4};
			bar1.addChord(pitchArray1c, C);

			
			//bar 2
	 		int[] pitchArray2a = {B1,G2,A3};
			CPhrase bar2 = new CPhrase(0.0);
			bar2.addChord(pitchArray2a, C);
	
	 		int[] pitchArray2b = {FS3};
			bar2.addChord(pitchArray2b, Q);
			
	 		int[] pitchArray2c = {G3};
			bar2.addChord(pitchArray2c, Q);	 

	 		int[] pitchArray2d = {B3};
			bar2.addChord(pitchArray2d, Q);	

	 		int[] pitchArray2e = {G4};
			bar2.addChord(pitchArray2e, Q);	


			//bar3
	 		int[] pitchArray3a = {C2,E2,G4};
			CPhrase bar3 = new CPhrase(0.0);
			bar3.addChord(pitchArray3a, C);

	 		int[] pitchArray3b = {C4};
			bar3.addChord(pitchArray3b, C);

	 		int[] pitchArray3c = {E4};
			bar3.addChord(pitchArray3c, C);
	
	
			//bar4
	 		int[] pitchArray4a = {G1,G4};
			CPhrase bar4 = new CPhrase(0.0);
			bar4.addChord(pitchArray4a, Q);

	 		int[] pitchArray4b = {B1};
			bar4.addChord(pitchArray4b, Q);

	 		int[] pitchArray4c = {G2,D4};
			bar4.addChord(pitchArray4c, C);

	 		int[] pitchArray4d = {B1};
			bar4.addChord(pitchArray4d, C);
	
	
			//bar5
	 		int[] pitchArray5a = {G1,G3,B3,D4,G4};
			CPhrase bar5 = new CPhrase(0.0);
			bar5.addChord(pitchArray5a, C);

	 		int[] pitchArray5b = {G2};
			bar5.addChord(pitchArray5b, Q);

	 		int[] pitchArray5c = {F2};
			bar5.addChord(pitchArray5c, Q);
			
			int[] pitchArray5d = {E2};
			bar5.addChord(pitchArray5d, Q);

	 		int[] pitchArray5e = {D2};
			bar5.addChord(pitchArray5e, Q);
			
			
			//bar6
	 		int[] pitchArray6a = {C2,E2,G3};
			CPhrase bar6 = new CPhrase(0.0);
			bar6.addChord(pitchArray6a, C);

	 		int[] pitchArray6b = {C4};
			bar6.addChord(pitchArray6b, C);

	 		int[] pitchArray6c = {E4};
			bar6.addChord(pitchArray6c, C);
	

			//bar7
	 		int[] pitchArray7a = {C2,G2,E4};
			CPhrase bar7 = new CPhrase(0.0);
			bar7.addChord(pitchArray7a, Q);

	 		int[] pitchArray7b = {C4};
			bar7.addChord(pitchArray7b, Q);

	 		int[] pitchArray7c = {E4};
			bar7.addChord(pitchArray7c, Q);
			
			int[] pitchArray7d = {G4};
			bar7.addChord(pitchArray7d, Q);

	 		int[] pitchArray7e = {C5};
			bar7.addChord(pitchArray7e, Q);
			
			int[] pitchArray7f = {G4};
			bar7.addChord(pitchArray7f, Q);


			//bar8
			int[] pitchArray8a = {C2,C4};
			CPhrase bar8 = new CPhrase(0.0);
			bar8.addChord(pitchArray8a, C);

	 		int[] pitchArray8b = {G1};
			bar8.addChord(pitchArray8b, C);

	 		int[] pitchArray8c = {C1};
			bar8.addChord(pitchArray8c, C);
			
			
			//bar9
	 		int[] pitchArray9a = {G2,C4,E4};
			CPhrase bar9 = new CPhrase(0.0);
			bar9.addChord(pitchArray9a, C);

	 		int[] pitchArray9b = {B3,D4};
			bar9.addChord(pitchArray9b, C);

	 		int[] pitchArray9c = {G1};
			bar9.addChord(pitchArray9c, C);
	

			//bar10
	 		int[] pitchArray10a = {G2,B3};
			CPhrase bar10 = new CPhrase(0.0);
			bar10.addChord(pitchArray10a, Q);

	 		int[] pitchArray10b = {A3};
			bar10.addChord(pitchArray10b, Q);

	 		int[] pitchArray10c = {B3};
			bar10.addChord(pitchArray10c, Q);
			
			int[] pitchArray10d = {C4};
			bar10.addChord(pitchArray10d, Q);

	 		int[] pitchArray10e = {D4};
			bar10.addChord(pitchArray10e, Q);
			
			int[] pitchArray10f = {B3};
			bar10.addChord(pitchArray10f, Q);

	
			//bar11
	 		int[] pitchArray11a = {C2, E4};
			CPhrase bar11 = new CPhrase(0.0);
			bar11.addChord(pitchArray11a, Q);

	 		int[] pitchArray11b = {C4};
			bar11.addChord(pitchArray11b, Q);

	 		int[] pitchArray11c = {D2,B3};
			bar11.addChord(pitchArray11c, Q);
			
			int[] pitchArray11d = {A3};
			bar11.addChord(pitchArray11d, Q);

	 		int[] pitchArray11e = {D1,G3};
			bar11.addChord(pitchArray11e, Q);
			
			int[] pitchArray11f = {FS3};
			bar11.addChord(pitchArray11f, Q);
	

			//bar12
	 		int[] pitchArray12a = {C2,E3,C4};
			CPhrase bar12 = new CPhrase(0.0);
			bar12.addChord(pitchArray12a, C);

	 		int[] pitchArray12b = {C2,E3,C4};
			bar12.addChord(pitchArray12b, C);

	 		int[] pitchArray12c = {C2,E3,C4};
			bar12.addChord(pitchArray12c, C);


			//bar13
	 		int[] pitchArray13a = {E2,G2,C4};
			CPhrase bar13 = new CPhrase(0.0);
			bar13.addChord(pitchArray13a, C);

	 		int[] pitchArray13b = {G3};
			bar13.addChord(pitchArray13b, C);

	 		int[] pitchArray13c = {E3};
			bar13.addChord(pitchArray13c, C);
			

			//bar14
			CPhrase bar14 = new CPhrase(0.0);
			bar14 = bar8.copy();	
			

			//bar15
	 		int[] pitchArray15a = {C2,G2,E4};
			CPhrase bar15 = new CPhrase(0.0);
			bar15.addChord(pitchArray15a, C);

	 		int[] pitchArray15b = {G4};
			bar15.addChord(pitchArray15b, Q);

	 		int[] pitchArray15c = {E4};
			bar15.addChord(pitchArray15c, Q);
			
			int[] pitchArray15d = {C2,E2,C4};
			bar15.addChord(pitchArray15d, C);


			//bar16
	 		int[] pitchArray16a = {D2,FS2,A4};
			CPhrase bar16 = new CPhrase(0.0);
			bar16.addChord(pitchArray16a, C);

	 		int[] pitchArray16b = {FS4};
			bar16.addChord(pitchArray16b, C);

	 		int[] pitchArray16c = {C2,FS2,D4};
			bar16.addChord(pitchArray16c, C);

					
			//bar17
	 		int[] pitchArray17a = {E2,G2,C4};
			CPhrase bar17 = new CPhrase(0.0);
			bar17.addChord(pitchArray17a, Q);

	 		int[] pitchArray17b = {G3};
			bar17.addChord(pitchArray17b, Q);

	 		int[] pitchArray17c = {C4};
			bar17.addChord(pitchArray17c, Q);
			
			int[] pitchArray17d = {E4};
			bar17.addChord(pitchArray17d, Q);

	 		int[] pitchArray17e = {G3};
			bar17.addChord(pitchArray17e, Q);
			
			int[] pitchArray17f = {C4};
			bar17.addChord(pitchArray17f, Q);
		

			//bar18
	 		int[] pitchArray18a = {C2,E2,G3};
			CPhrase bar18 = new CPhrase(0.0);
			bar18.addChord(pitchArray18a, C);

	 		int[] pitchArray18b = {C4};
			bar18.addChord(pitchArray18b, C);

	 		int[] pitchArray18c = {C2,G2,E4};
			bar18.addChord(pitchArray18c, C);


			//bar19
	 		int[] pitchArray19a = {C2,G2,E4};
			CPhrase bar19 = new CPhrase(0.0);
			bar19.addChord(pitchArray19a, Q);

	 		int[] pitchArray19b = {C4};
			bar19.addChord(pitchArray19b, Q);

	 		int[] pitchArray19c = {E4};
			bar19.addChord(pitchArray19c, C);
			
			int[] pitchArray19d = {C2,E2,G4};
			bar19.addChord(pitchArray19d, C);


			//bar20
	 		int[] pitchArray20a = {B1,G4};
			CPhrase bar20 = new CPhrase(0.0);
			bar20.addChord(pitchArray20a, C);

	 		int[] pitchArray20b = {B4};
			bar20.addChord(pitchArray20b, Q);

	 		int[] pitchArray20c = {D5};
			bar20.addChord(pitchArray20c, Q);
			
			int[] pitchArray20d = {D4};
			bar20.addChord(pitchArray20d, C);

		
			//bar21
	 		int[] pitchArray21a = {C2,C4};
			CPhrase bar21 = new CPhrase(0.0);
			bar21.addChord(pitchArray21a, Q);

	 		int[] pitchArray21b = {E4};
			bar21.addChord(pitchArray21b, Q);

	 		int[] pitchArray21c = {D2,G4};
			bar21.addChord(pitchArray21c, Q);
			
			int[] pitchArray21d = {D4};
			bar21.addChord(pitchArray21d, Q);

	 		int[] pitchArray21e = {D1,A3};
			bar21.addChord(pitchArray21e, Q);
			
			int[] pitchArray21f = {FS4};
			bar21.addChord(pitchArray21f, Q);
		
		
			//bar22
	 		int[] pitchArray22a = {C2,E4};
			CPhrase bar22 = new CPhrase(0.0);
			bar22.addChord(pitchArray22a, C);

	 		int[] pitchArray22b = {C4};
			bar22.addChord(pitchArray22b, C);

	 		int[] pitchArray22c = {G3};
			bar22.addChord(pitchArray22c, C);


			//bar23
	 		int[] pitchArray23a = {F2,F4};
			CPhrase bar23 = new CPhrase(0.0);
			bar23.addChord(pitchArray23a, Q);

	 		int[] pitchArray23b = {E2,E4};
			bar23.addChord(pitchArray23b, Q);

	 		int[] pitchArray23c = {D2,D4};
			bar23.addChord(pitchArray23c, Q);
			
			int[] pitchArray23d = {E2,E4};
			bar23.addChord(pitchArray23d, Q);

	 		int[] pitchArray23e = {F2,F4};
			bar23.addChord(pitchArray23e, Q);
			
			int[] pitchArray23f = {G2,G4};
			bar23.addChord(pitchArray23f, Q);
			
			
			//bar24
			CPhrase bar24 = new CPhrase(0.0);
			bar24 = bar5.copy();	
	

			//bar25
	 		int[] pitchArray25a = {D2,D3};
			CPhrase bar25 = new CPhrase(0.0);
			bar25.addChord(pitchArray25a, Q);

	 		int[] pitchArray25b = {FS3};
			bar25.addChord(pitchArray25b, Q);

	 		int[] pitchArray25c = {A3};
			bar25.addChord(pitchArray25c, Q);
			
			int[] pitchArray25d = {D4};
			bar25.addChord(pitchArray25d, Q);

	 		int[] pitchArray25e = {C2,FS4};
			bar25.addChord(pitchArray25e, Q);
			
			int[] pitchArray25f = {A4};
			bar25.addChord(pitchArray25f, Q);

	
			//bar26
	 		int[] pitchArray26a = {C2,C4,E4};
			CPhrase bar26 = new CPhrase(0.0);
			bar26.addChord(pitchArray26a, Q);

	 		int[] pitchArray26b = {E2};
			bar26.addChord(pitchArray26b, Q);

	 		int[] pitchArray26c = {G2,C4,E4};
			bar26.addChord(pitchArray26c, Q);
			
			int[] pitchArray26d = {E2};
			bar26.addChord(pitchArray26d, Q);

	 		int[] pitchArray26e = {C3,C4,E4};
			bar26.addChord(pitchArray26e, Q);
			
			int[] pitchArray26f = {C2};
			bar26.addChord(pitchArray26f, Q);

		
			//bar27
	 		int[] pitchArray27a = {G2,B2,F4};
			CPhrase bar27 = new CPhrase(0.0);
			bar27.addChord(pitchArray27a, Q);

	 		int[] pitchArray27b = {E4};
			bar27.addChord(pitchArray27b, Q);

	 		int[] pitchArray27c = {F4};
			bar27.addChord(pitchArray27c, Q);
			
			int[] pitchArray27d = {D4};
			bar27.addChord(pitchArray27d, Q);

	 		int[] pitchArray27e = {C4};
			bar27.addChord(pitchArray27e, Q);
			
			int[] pitchArray27f = {B3};
			bar27.addChord(pitchArray27f, Q);


			//bar28
	 		int[] pitchArray28a = {C2,A2,FS4};
			CPhrase bar28 = new CPhrase(0.0);
			bar28.addChord(pitchArray28a, Q);

	 		int[] pitchArray28b = {D4};
			bar28.addChord(pitchArray28b, Q);

	 		int[] pitchArray28c = {A3};
			bar28.addChord(pitchArray28c, Q);
			
			int[] pitchArray28d = {A4};
			bar28.addChord(pitchArray28d, Q);

	 		int[] pitchArray28e = {FS4};
			bar28.addChord(pitchArray28e, Q);
			
			int[] pitchArray28f = {D4};
			bar28.addChord(pitchArray28f, Q);


			//bar29
	 		int[] pitchArray29a = {G2,B3};
			CPhrase bar29 = new CPhrase(0.0);
			bar29.addChord(pitchArray29a, Q);

	 		int[] pitchArray29b = {D4};
			bar29.addChord(pitchArray29b, Q);

	 		int[] pitchArray29c = {G4};
			bar29.addChord(pitchArray29c, Q);
			
			int[] pitchArray29d = {D4};
			bar29.addChord(pitchArray29d, Q);

	 		int[] pitchArray29e = {G1,B3};
			bar29.addChord(pitchArray29e, C);

					
			//bar30
			CPhrase bar30 = new CPhrase(0.0);
			bar30 = bar5.copy();
				

			//bar31
	 		int[] pitchArray31a = {C2,G2,E4};
			CPhrase bar31 = new CPhrase(0.0);
			bar31.addChord(pitchArray31a, Q);

	 		int[] pitchArray31b = {C4};
			bar31.addChord(pitchArray31b, Q);

	 		int[] pitchArray31c = {G3};
			bar31.addChord(pitchArray31c, Q);
			
			int[] pitchArray31d = {C2,G2,E4};
			bar31.addChord(pitchArray31d, Q);

					
			//bar32
			int[] pitchArray32a = {C2,E2,G3};
			CPhrase bar32 = new CPhrase(0.0);
			bar32.addChord(pitchArray32a, C);

	 		int[] pitchArray32b = {C4};
			bar32.addChord(pitchArray32b, C);

	 		int[] pitchArray32c = {E4};
			bar32.addChord(pitchArray32c, C);


			//bar33
			CPhrase bar33 = new CPhrase(0.0);
			bar33 = bar5.copy();
	

			//bar34
	 		int[] pitchArray34a = {G2,E4};
			CPhrase bar34 = new CPhrase(0.0);
			bar34.addChord(pitchArray34a, Q);

	 		int[] pitchArray34b = {C4};
			bar34.addChord(pitchArray34b, Q);

	 		int[] pitchArray34c = {D4};
			bar34.addChord(pitchArray34c, Q);
			
			int[] pitchArray34d = {B3};
			bar34.addChord(pitchArray34d, Q);

	 		int[] pitchArray34e = {G3};
			bar34.addChord(pitchArray34e, C);


			//bar35
	 		int[] pitchArray35a = {D2,FS2,A3};
			CPhrase bar35 = new CPhrase(0.0);
			bar35.addChord(pitchArray35a, C);

	 		int[] pitchArray35b = {D4};
			bar35.addChord(pitchArray35b, C);

	 		int[] pitchArray35c = {C2,A2,FS4};
			bar35.addChord(pitchArray35c, C);

			
			//bar36
	 		int[] pitchArray36a = {C2,A3};
			CPhrase bar36 = new CPhrase(0.0);
			bar36.addChord(pitchArray36a, Q);

	 		int[] pitchArray36b = {E4};
			bar36.addChord(pitchArray36b, Q);

	 		int[] pitchArray36c = {D2,D4};
			bar36.addChord(pitchArray36c, Q);
			
			int[] pitchArray36d = {G4};
			bar36.addChord(pitchArray36d, Q);

	 		int[] pitchArray36e = {D1,FS4};
			bar36.addChord(pitchArray36e, Q);
			
			int[] pitchArray36f = {A4};
			bar36.addChord(pitchArray36f, Q);


			//bar37
	 		int[] pitchArray37a = {B1,D2,G4};
			CPhrase bar37 = new CPhrase(0.0);
			bar37.addChord(pitchArray37a, Q);

	 		int[] pitchArray37b = {B4};
			bar37.addChord(pitchArray37b, Q);

	 		int[] pitchArray37c = {G4};
			bar37.addChord(pitchArray37c, Q);
			
			int[] pitchArray37d = {D4};
			bar37.addChord(pitchArray37d, Q);

	 		int[] pitchArray37e = {B3};
			bar37.addChord(pitchArray37e, C);
			

			//bar38
	 		int[] pitchArray38a = {C2,E2,C4};
			CPhrase bar38 = new CPhrase(0.0);
			bar38.addChord(pitchArray38a, Q);

	 		int[] pitchArray38b = {G2};
			bar38.addChord(pitchArray38b, Q);

	 		int[] pitchArray38c = {C2,E2,G3};
			bar38.addChord(pitchArray38c, Q);
			
			int[] pitchArray38d = {G2};
			bar38.addChord(pitchArray38d, Q);

	 		int[] pitchArray38e = {C2,E2,E4};
			bar38.addChord(pitchArray38e, Q);
			
			int[] pitchArray38f = {G2};
			bar38.addChord(pitchArray38f, Q);


			//bar39
	 		int[] pitchArray39a = {B1,G4};
			CPhrase bar39 = new CPhrase(0.0);
			bar39.addChord(pitchArray39a, Q);

	 		int[] pitchArray39b = {D2};
			bar39.addChord(pitchArray39b, Q);

	 		int[] pitchArray39c = {G2,G3};
			bar39.addChord(pitchArray39c, Q);
			
			int[] pitchArray39d = {D2};
			bar39.addChord(pitchArray39d, Q);

	 		int[] pitchArray39e = {B1,G3};
			bar39.addChord(pitchArray39e, Q);
			
			int[] pitchArray39f = {G1};
			bar39.addChord(pitchArray39f, Q);

			//bar40
	 		int[] pitchArray40a = {C2,E2,C4};
			CPhrase bar40 = new CPhrase(0.0);
			bar40.addChord(pitchArray40a, Q);

	 		int[] pitchArray40b = {B3};
			bar40.addChord(pitchArray40b, Q);

	 		int[] pitchArray40c = {C4};
			bar40.addChord(pitchArray40c, Q);
			
			int[] pitchArray40d = {E4};
			bar40.addChord(pitchArray40d, Q);

	 		int[] pitchArray40e = {G3};
			bar40.addChord(pitchArray40e, Q);
			
			int[] pitchArray40f = {C4};
			bar40.addChord(pitchArray40f, Q);


			//bar41
	 		int[] pitchArray41a = {C2,E2,C4};
			CPhrase bar41 = new CPhrase(0.0);
			bar41.addChord(pitchArray41a, Q);

	 		int[] pitchArray41b = {B3};
			bar41.addChord(pitchArray41b, Q);

	 		int[] pitchArray41c = {C4};
			bar41.addChord(pitchArray41c, Q);
			
			int[] pitchArray41d = {E4};
			bar41.addChord(pitchArray41d, Q);

	 		int[] pitchArray41e = {G3};
			bar41.addChord(pitchArray41e, C);
			

			//bar42
	 		int[] pitchArray42a = {G1,B3};
			CPhrase bar42 = new CPhrase(0.0);
			bar42.addChord(pitchArray42a, Q);

	 		int[] pitchArray42b = {C4};
			bar42.addChord(pitchArray42b, Q);

	 		int[] pitchArray42c = {D4};
			bar42.addChord(pitchArray42c, Q);
			
			int[] pitchArray42d = {B3};
			bar42.addChord(pitchArray42d, Q);

	 		int[] pitchArray42e = {A3};
			bar42.addChord(pitchArray42e, Q);
			
			int[] pitchArray42f = {G3};
			bar42.addChord(pitchArray42f, Q);


			//bar43
	 		int[] pitchArray43a = {C2,E2,G4};
			CPhrase bar43 = new CPhrase(0.0);
			bar43.addChord(pitchArray43a, C);

	 		int[] pitchArray43b = {F4};
			bar43.addChord(pitchArray43b, Q);

	 		int[] pitchArray43c = {E4};
			bar43.addChord(pitchArray43c, Q);
			
			int[] pitchArray43d = {D4};
			bar43.addChord(pitchArray43d, Q);

	 		int[] pitchArray43e = {C4};
			bar43.addChord(pitchArray43e, Q);


			//bar44
	 		int[] pitchArray44a = {F2,A3};
			CPhrase bar44 = new CPhrase(0.0);
			bar44.addChord(pitchArray44a, C);

	 		int[] pitchArray44b = {F4};
			bar44.addChord(pitchArray44b, Q);

	 		int[] pitchArray44c = {D4};
			bar44.addChord(pitchArray44c, Q);
			
			int[] pitchArray44d = {G2,A3};
			bar44.addChord(pitchArray44d, Q);

	 		int[] pitchArray44e = {B3};
			bar44.addChord(pitchArray44e, Q);
			
			
			//bar45
	 		int[] pitchArray45a = {E2,G2,C4};
			CPhrase bar45 = new CPhrase(0.0);
			bar45.addChord(pitchArray45a, Q);

	 		int[] pitchArray45b = {B3};
			bar45.addChord(pitchArray45b, Q);

	 		int[] pitchArray45c = {C4};
			bar45.addChord(pitchArray45c, Q);
			
			int[] pitchArray45d = {G3};
			bar45.addChord(pitchArray45d, Q);

	 		int[] pitchArray45e = {E3};
			bar45.addChord(pitchArray45e, Q);
			
			int[] pitchArray45f = {C3};
			bar45.addChord(pitchArray45f, Q);


			//bar46
	 		int[] pitchArray46a = {B1,D2,G4};
			CPhrase bar46 = new CPhrase(0.0);
			bar46.addChord(pitchArray46a, C);

	 		int[] pitchArray46b = {B4};
			bar46.addChord(pitchArray46b, Q);

	 		int[] pitchArray46c = {G4};
			bar46.addChord(pitchArray46c, Q);
			
			int[] pitchArray46d = {D4};
			bar46.addChord(pitchArray46d, Q);

	 		int[] pitchArray46e = {B3};
			bar46.addChord(pitchArray46e, Q);


			//bar47
	 		int[] pitchArray47a = {B1,D2,G4};
			CPhrase bar47 = new CPhrase(0.0);
			bar47.addChord(pitchArray47a, C);

	 		int[] pitchArray47b = {G4};
			bar47.addChord(pitchArray47b, Q);

	 		int[] pitchArray47c = {D4};
			bar47.addChord(pitchArray47c, Q);
			
			int[] pitchArray47d = {B4};
			bar47.addChord(pitchArray47d, C);


			//bar48
	 		int[] pitchArray48a = {C2,G2,E4};
			CPhrase bar48 = new CPhrase(0.0);
			bar48.addChord(pitchArray48a, C);

	 		int[] pitchArray48b = {C4};
			bar48.addChord(pitchArray48b, Q);

	 		int[] pitchArray48c = {E4};
			bar48.addChord(pitchArray48c, Q);
			
			int[] pitchArray48d = {C2,E2,G4};
			bar48.addChord(pitchArray48d, Q);

	 		int[] pitchArray48e = {C5};
			bar48.addChord(pitchArray48e, Q);


			//bar49
	 		int[] pitchArray49a = {C2,E2,E4};
			CPhrase bar49 = new CPhrase(0.0);
			bar49.addChord(pitchArray49a, Q);

	 		int[] pitchArray49b = {G2};
			bar49.addChord(pitchArray49b, Q);

	 		int[] pitchArray49c = {C2,E2,C4};
			bar49.addChord(pitchArray49c, Q);
			
			int[] pitchArray49d = {G2};
			bar49.addChord(pitchArray49d, Q);

	 		int[] pitchArray49e = {C2,E2,G3};
			bar49.addChord(pitchArray49e, Q);
			
			int[] pitchArray49f = {G2};
			bar49.addChord(pitchArray49f, Q);


			//bar50
	 		int[] pitchArray50a = {E2,G2,C4};
			CPhrase bar50 = new CPhrase(0.0);
			bar50.addChord(pitchArray50a, C);

	 		int[] pitchArray50b = {E4};
			bar50.addChord(pitchArray50b, Q);

	 		int[] pitchArray50c = {C4};
			bar50.addChord(pitchArray50c, Q);
			
			int[] pitchArray50d = {G3};
			bar50.addChord(pitchArray50d, C);


			//bar51
	 		int[] pitchArray51a = {C2,E2,C4};
			CPhrase bar51 = new CPhrase(0.0);
			bar51.addChord(pitchArray51a, Q);

	 		int[] pitchArray51b = {G3};
			bar51.addChord(pitchArray51b, Q);

	 		int[] pitchArray51c = {E4};
			bar51.addChord(pitchArray51c, Q);
			
			int[] pitchArray51d = {C4};
			bar51.addChord(pitchArray51d, Q);

	 		int[] pitchArray51e = {G4};
			bar51.addChord(pitchArray51e, Q);
			
			int[] pitchArray51f = {E4};
			bar51.addChord(pitchArray51f, Q);


			//bar52
	 		int[] pitchArray52a = {F2,D4};
			CPhrase bar52 = new CPhrase(0.0);
			bar52.addChord(pitchArray52a, Q);

	 		int[] pitchArray52b = {cs4};
			bar52.addChord(pitchArray52b, Q);

	 		int[] pitchArray52c = {D4};
			bar52.addChord(pitchArray52c, Q);
			
			int[] pitchArray52d = {F4};
			bar52.addChord(pitchArray52d, Q);

	 		int[] pitchArray52e = {G2,G3};
			bar52.addChord(pitchArray52e, Q);
			
			int[] pitchArray52f = {B3};
			bar52.addChord(pitchArray52f, Q);
			
			
			//bar53
	 		int[] pitchArray53a = {C2,C4,E4};
			CPhrase bar53 = new CPhrase(0.0);
			bar53.addChord(pitchArray53a, C);

	 		int[] pitchArray53b = {C4,E4};
			bar53.addChord(pitchArray53b, Q);

	 		int[] pitchArray53c = {D4,F4};
			bar53.addChord(pitchArray53c, Q);
			
			int[] pitchArray53d = {E4,G4};
			bar53.addChord(pitchArray53d, C);


			//bar54
			int[] pitchArray54a = {C2,E3,C4};
			CPhrase bar54 = new CPhrase(0.0);
			bar54.addChord(pitchArray54a, C);

	 		int[] pitchArray54b = {C2,E3,C4};
			bar54.addChord(pitchArray54b, C);

	 		int[] pitchArray54c = {C2,E3,C4};
			bar54.addChord(pitchArray54c, C);


			//bar55
	 		int[] pitchArray55a = {B1,D2,G4};
			CPhrase bar55 = new CPhrase(0.0);
			bar55.addChord(pitchArray55a, C);

	 		int[] pitchArray55b = {B4};
			bar55.addChord(pitchArray55b, C);

	 		int[] pitchArray55c = {D4};
			bar55.addChord(pitchArray55c, C);


			//bar56
	 		int[] pitchArray56a = {G1,G2,D4};
			CPhrase bar56 = new CPhrase(0.0);
			bar56.addChord(pitchArray56a, Q);

	 		int[] pitchArray56b = {B3};
			bar56.addChord(pitchArray56b, Q);

	 		int[] pitchArray56c = {G3};
			bar56.addChord(pitchArray56c, C);
			
			int[] pitchArray56d = {G2};
			bar56.addChord(pitchArray56d, C);


			//bar57
			CPhrase bar57 = new CPhrase(0.0);
			bar57 = bar49.copy();
			

			//bar58
	 		int[] pitchArray58a = {C2,E2,G4};
			CPhrase bar58 = new CPhrase(0.0);
			bar58.addChord(pitchArray58a, Q);

	 		int[] pitchArray58b = {G2};
			bar58.addChord(pitchArray58b, Q);

	 		int[] pitchArray58c = {C2,E2,E4};
			bar58.addChord(pitchArray58c, Q);
			
			int[] pitchArray58d = {G2};
			bar58.addChord(pitchArray58d, Q);

	 		int[] pitchArray58e = {C2,E2,C4};
			bar58.addChord(pitchArray58e, Q);
			
			int[] pitchArray58f = {G2};
			bar58.addChord(pitchArray58f, Q);
			
			
			//bar59
	 		int[] pitchArray59a = {C2,E2,G4};
			CPhrase bar59 = new CPhrase(0.0);
			bar59.addChord(pitchArray59a, Q);

	 		int[] pitchArray59b = {G2};
			bar59.addChord(pitchArray59b, Q);

	 		int[] pitchArray59c = {C2,E2,C4};
			bar59.addChord(pitchArray59c, Q);
			
			int[] pitchArray59d = {G2};
			bar59.addChord(pitchArray59d, Q);

	 		int[] pitchArray59e = {C2,E2,E4};
			bar59.addChord(pitchArray59e, Q);
			
			int[] pitchArray59f = {G2};
			bar59.addChord(pitchArray59f, Q);

			
			//bar60
	 		int[] pitchArray60a = {C2,E2,G4};
			CPhrase bar60 = new CPhrase(0.0);
			bar60.addChord(pitchArray60a, C);

	 		int[] pitchArray60b = {F4};
			bar60.addChord(pitchArray60b, Q);

	 		int[] pitchArray60c = {E4};
			bar60.addChord(pitchArray60c, Q);
			
			int[] pitchArray60d = {D4};
			bar60.addChord(pitchArray60d, Q);

	 		int[] pitchArray60e = {C4};
			bar60.addChord(pitchArray60e, Q);


			//bar61
	 		int[] pitchArray61a = {E2,G2,C4};
			CPhrase bar61 = new CPhrase(0.0);
			bar61.addChord(pitchArray61a, C);

	 		int[] pitchArray61b = {E4};
			bar61.addChord(pitchArray61b, Q);

	 		int[] pitchArray61c = {C4};
			bar61.addChord(pitchArray61c, Q);
			
			int[] pitchArray61d = {G4};
			bar61.addChord(pitchArray61d, C);


			//bar62
	 		int[] pitchArray62a = {C2,E4};
			CPhrase bar62 = new CPhrase(0.0);
			bar62.addChord(pitchArray62a, Q);

	 		int[] pitchArray62b = {C4};
			bar62.addChord(pitchArray62b, Q);

	 		int[] pitchArray62c = {D2,B3};
			bar62.addChord(pitchArray62c, Q);
			
			int[] pitchArray62d = {G3};
			bar62.addChord(pitchArray62d, Q);

	 		int[] pitchArray62e = {D1,A3};
			bar62.addChord(pitchArray62e, Q);
			
			int[] pitchArray62f = {FS3};
			bar62.addChord(pitchArray62f, Q);


			//bar63
	 		int[] pitchArray63a = {C2,E4};
			CPhrase bar63 = new CPhrase(0.0);
			bar63.addChord(pitchArray63a, Q);

	 		int[] pitchArray63b = {C4};
			bar63.addChord(pitchArray63b, Q);

	 		int[] pitchArray63c = {B3};
			bar63.addChord(pitchArray63c, Q);
			
			int[] pitchArray63d = {C4};
			bar63.addChord(pitchArray63d, Q);

	 		int[] pitchArray63e = {G3};
			bar63.addChord(pitchArray63e, C);


			//bar64
	 		int[] pitchArray64a = {C2,G2,E4};
			CPhrase bar64 = new CPhrase(0.0);
			bar64.addChord(pitchArray64a, Q);

	 		int[] pitchArray64b = {G4};
			bar64.addChord(pitchArray64b, Q);

	 		int[] pitchArray64c = {C4};
			bar64.addChord(pitchArray64c, Q);
			
			int[] pitchArray64d = {G4};
			bar64.addChord(pitchArray64d, Q);

	 		int[] pitchArray64e = {C2,G2,E4};
			bar64.addChord(pitchArray64e, Q);
			
			int[] pitchArray64f = {C4};
			bar64.addChord(pitchArray64f, Q);


			//bar65
	 		int[] pitchArray65a = {D2,FS2,D4};
			CPhrase bar65 = new CPhrase(0.0);
			bar65.addChord(pitchArray65a, Q);

	 		int[] pitchArray65b = {A3};
			bar65.addChord(pitchArray65b, Q);

	 		int[] pitchArray65c = {D4};
			bar65.addChord(pitchArray65c, C);
			
			int[] pitchArray65d = {FS4};
			bar65.addChord(pitchArray65d, C);


			//bar66
	 		int[] pitchArray66a = {D2,A2,FS4};
			CPhrase bar66 = new CPhrase(0.0);
			bar66.addChord(pitchArray66a, C);

	 		int[] pitchArray66b = {D2,FS2,A4};
			bar66.addChord(pitchArray66b, C);

	 		int[] pitchArray66c = {C2,D2,FS4};
			bar66.addChord(pitchArray66c, C);


			//bar67
	 		int[] pitchArray67a = {C2,E2,C4};
			CPhrase bar67 = new CPhrase(0.0);
			bar67.addChord(pitchArray67a, Q);

	 		int[] pitchArray67b = {B3};
			bar67.addChord(pitchArray67b, Q);

	 		int[] pitchArray67c = {C4};
			bar67.addChord(pitchArray67c, Q);
			
			int[] pitchArray67d = {E4};
			bar67.addChord(pitchArray67d, Q);

	 		int[] pitchArray67e = {E2,G2,G3};
			bar67.addChord(pitchArray67e, Q);
			
			int[] pitchArray67f = {C4};
			bar67.addChord(pitchArray67f, Q);


			//bar68
	 		int[] pitchArray68a = {B1,G4};
			CPhrase bar68 = new CPhrase(0.0);
			bar68.addChord(pitchArray68a, C);

	 		int[] pitchArray68b = {B4};
			bar68.addChord(pitchArray68b, Q);

	 		int[] pitchArray68c = {G4};
			bar68.addChord(pitchArray68c, Q);
			
			int[] pitchArray68d = {D4};
			bar68.addChord(pitchArray68d, Q);

	 		int[] pitchArray68e = {G4};
			bar68.addChord(pitchArray68e, Q);


			//bar69
	 		int[] pitchArray69a = {C2,E2,G4};
			CPhrase bar69 = new CPhrase(0.0);
			bar69.addChord(pitchArray69a, C);

	 		int[] pitchArray69b = {E4};
			bar69.addChord(pitchArray69b, C);

	 		int[] pitchArray69c = {C4};
			bar69.addChord(pitchArray69c, C);
	

			//bar70
	 		int[] pitchArray70a = {D2,FS4};
			CPhrase bar70 = new CPhrase(0.0);
			bar70.addChord(pitchArray70a, C);

	 		int[] pitchArray70b = {A4};
			bar70.addChord(pitchArray70b, Q);

	 		int[] pitchArray70c = {FS4};
			bar70.addChord(pitchArray70c, Q);
			
			int[] pitchArray70d = {C2,D4};
			bar70.addChord(pitchArray70d, Q);

	 		int[] pitchArray70e = {FS4};
			bar70.addChord(pitchArray70e, Q);


			//bar71
	 		int[] pitchArray71a = {B1,D2,G4};
			CPhrase bar71 = new CPhrase(0.0);
			bar71.addChord(pitchArray71a, Q);

	 		int[] pitchArray71b = {B4};
			bar71.addChord(pitchArray71b, Q);

	 		int[] pitchArray71c = {D5};
			bar71.addChord(pitchArray71c, Q);
			
			int[] pitchArray71d = {B4};
			bar71.addChord(pitchArray71d, Q);

	 		int[] pitchArray71e = {B1,D2,G4};
			bar71.addChord(pitchArray71e, C);


			//bar72
	 		int[] pitchArray72a = {F2,F4};
			CPhrase bar72 = new CPhrase(0.0);
			bar72.addChord(pitchArray72a, Q);

	 		int[] pitchArray72b = {E4};
			bar72.addChord(pitchArray72b, Q);

	 		int[] pitchArray72c = {D4};
			bar72.addChord(pitchArray72c, Q);
			
			int[] pitchArray72d = {C4};
			bar72.addChord(pitchArray72d, Q);

	 		int[] pitchArray72e = {G2,B3};
			bar72.addChord(pitchArray72e, Q);
			
			int[] pitchArray72f = {D4};
			bar72.addChord(pitchArray72f, Q);
			

			//bar73
	 		int[] pitchArray73a = {C2,E2,G4};
			CPhrase bar73 = new CPhrase(0.0);
			bar73.addChord(pitchArray73a, Q);

	 		int[] pitchArray73b = {G4};
			bar73.addChord(pitchArray73b, Q);

	 		int[] pitchArray73c = {C2,E2,E4};
			bar73.addChord(pitchArray73c, Q);
			
			int[] pitchArray73d = {G4};
			bar73.addChord(pitchArray73d, Q);

	 		int[] pitchArray73e = {C2,E2,C4};
			bar73.addChord(pitchArray73e, Q);
			
			int[] pitchArray73f = {G4};
			bar73.addChord(pitchArray73f, Q);

				
			//bar74
	 		int[] pitchArray74a = {C2,E2,C5};
			CPhrase bar74 = new CPhrase(0.0);
			bar74.addChord(pitchArray74a, Q);

	 		int[] pitchArray74b = {B4};
			bar74.addChord(pitchArray74b, Q);

	 		int[] pitchArray74c = {C5};
			bar74.addChord(pitchArray74c, Q);
			
			int[] pitchArray74d = {G4};
			bar74.addChord(pitchArray74d, Q);

	 		int[] pitchArray74e = {E4};
			bar74.addChord(pitchArray74e, Q);
			
			int[] pitchArray74f = {C4};
			bar74.addChord(pitchArray74f, Q);


			//bar75
	 		int[] pitchArray75a = {C2,D4,FS4};
			CPhrase bar75 = new CPhrase(0.0);
			bar75.addChord(pitchArray75a, C);

	 		int[] pitchArray75b = {C2,D4,FS4};
			bar75.addChord(pitchArray75b, C);

	 		int[] pitchArray75c = {C2,D4,FS4};
			bar75.addChord(pitchArray75c, C);


			//bar76
	 		int[] pitchArray76a = {C2,E2,C4};
			CPhrase bar76 = new CPhrase(0.0);
			bar76.addChord(pitchArray76a, Q);

	 		int[] pitchArray76b = {B3};
			bar76.addChord(pitchArray76b, Q);

	 		int[] pitchArray76c = {C4};
			bar76.addChord(pitchArray76c, Q);
			
			int[] pitchArray76d = {G4};
			bar76.addChord(pitchArray76d, Q);

	 		int[] pitchArray76e = {C2,G2,E4};
			bar76.addChord(pitchArray76e, Q);
			
			int[] pitchArray76f = {C4};
			bar76.addChord(pitchArray76f, Q);


			//bar77
	 		int[] pitchArray77a = {B1,D2,G4};
			CPhrase bar77 = new CPhrase(0.0);
			bar77.addChord(pitchArray77a, Q);

	 		int[] pitchArray77b = {B4};
			bar77.addChord(pitchArray77b, Q);

	 		int[] pitchArray77c = {G4};
			bar77.addChord(pitchArray77c, C);
			
			int[] pitchArray77d = {B1,G2,D4};
			bar77.addChord(pitchArray77d, C);


			//bar 78
	 		int[] pitchArray78a = {C2,C4};
			CPhrase bar78 = new CPhrase(0.0);
			bar78.addChord(pitchArray78a, C);
			
			int[] pitchArray78b = {C3};
			bar78.addChord(pitchArray78b, C);

			int[] pitchArray78c = {C1};
			bar78.addChord(pitchArray78c, C);
			
			
			//bar79
			CPhrase bar79 = new CPhrase(0.0);
			bar79 = bar8.copy();	
			
			
			//bar80
	 		int[] pitchArray80a = {C2,D4};
			CPhrase bar80 = new CPhrase(0.0);
			bar80.addChord(pitchArray80a, C);

	 		int[] pitchArray80b = {A3};
			bar80.addChord(pitchArray80b, C);

	 		int[] pitchArray80c = {FS4};
			bar80.addChord(pitchArray80c, C);


			//bar81
			CPhrase bar81 = new CPhrase(0.0);
			bar81 = bar5.copy();
		

			//bar82
	 		int[] pitchArray82a = {B1,G2,D4};
			CPhrase bar82 = new CPhrase(0.0);
			bar82.addChord(pitchArray82a, Q);

	 		int[] pitchArray82b = {B3};
			bar82.addChord(pitchArray82b, Q);

	 		int[] pitchArray82c = {G3};
			bar82.addChord(pitchArray82c, C);
			
			int[] pitchArray82d = {B1,D2,G4};
			bar82.addChord(pitchArray82d, C);


			//bar83
			CPhrase bar83 = new CPhrase(0.0);
			bar83 = bar8.copy();

			
			//bar84
	 		int[] pitchArray84a = {C2,E2,C4};
			CPhrase bar84 = new CPhrase(0.0);
			bar84.addChord(pitchArray84a, Q);

	 		int[] pitchArray84b = {G3};
			bar84.addChord(pitchArray84b, Q);

	 		int[] pitchArray84c = {E4};
			bar84.addChord(pitchArray84c, Q);
			
			int[] pitchArray84d = {C4};
			bar84.addChord(pitchArray84d, Q);

	 		int[] pitchArray84e = {G4};
			bar84.addChord(pitchArray84e, Q);
			
			int[] pitchArray84f = {E4};
			bar84.addChord(pitchArray84f, Q);


			//bar85
	 		int[] pitchArray85a = {E2,G2,C4};
			CPhrase bar85 = new CPhrase(0.0);
			bar85.addChord(pitchArray85a, Q);

	 		int[] pitchArray85b = {E4};
			bar85.addChord(pitchArray85b, Q);

	 		int[] pitchArray85c = {G3};
			bar85.addChord(pitchArray85c, Q);


			//bar86
	 		int[] pitchArray86a = {B1,G2,D4};
			CPhrase bar86 = new CPhrase(0.0);
			bar86.addChord(pitchArray86a, C);

	 		int[] pitchArray86b = {D4};
			bar86.addChord(pitchArray86b, Q);

	 		int[] pitchArray86c = {G4};
			bar86.addChord(pitchArray86c, Q);
			
			int[] pitchArray86d = {B4};
			bar86.addChord(pitchArray86d, C);


			//bar 87
	 		int[] pitchArray87a = {C2,E2,G4};
			CPhrase bar87 = new CPhrase(0.0);
			bar87.addChord(pitchArray87a, C);
			
			int[] pitchArray87b = {C4};
			bar87.addChord(pitchArray87b, C);

			int[] pitchArray87c = {C2,G2,E4};
			bar87.addChord(pitchArray87c, C);
			
			//bar88
	 		int[] pitchArray88a = {B1,D2,G4};
			CPhrase bar88 = new CPhrase(0.0);
			bar88.addChord(pitchArray88a, Q);

	 		int[] pitchArray88b = {D4};
			bar88.addChord(pitchArray88b, Q);

	 		int[] pitchArray88c = {G4};
			bar88.addChord(pitchArray88c, Q);
			
			int[] pitchArray88d = {B4};
			bar88.addChord(pitchArray88d, Q);

	 		int[] pitchArray88e = {B1,D2,G4};
			bar88.addChord(pitchArray88e, Q);
			
			int[] pitchArray88f = {D4};
			bar88.addChord(pitchArray88f, Q);


			//bar89
	 		int[] pitchArray89a = {F2,F4};
			CPhrase bar89 = new CPhrase(0.0);
			bar89.addChord(pitchArray89a, Q);

	 		int[] pitchArray89b = {E2,E4};
			bar89.addChord(pitchArray89b, Q);

	 		int[] pitchArray89c = {D2,D4};
			bar89.addChord(pitchArray89c, C);
			
			int[] pitchArray89d = {G2,G4};
			bar89.addChord(pitchArray89d, C);
			

			//bar90
	 		int[] pitchArray90a = {C2,A2,FS4};
			CPhrase bar90 = new CPhrase(0.0);
			bar90.addChord(pitchArray90a, Q);

	 		int[] pitchArray90b = {A4};
			bar90.addChord(pitchArray90b, Q);

	 		int[] pitchArray90c = {D5};
			bar90.addChord(pitchArray90c, Q);
			
			int[] pitchArray90d = {A4};
			bar90.addChord(pitchArray90d, Q);

	 		int[] pitchArray90e = {C2,A2,FS4};
			bar90.addChord(pitchArray90e, Q);
			
			int[] pitchArray90f = {A4};
			bar90.addChord(pitchArray90f, Q);

						
			//bar91
			CPhrase bar91 = new CPhrase(0.0);
			bar91 = bar5.copy();
			

			//bar92
	 		int[] pitchArray92a = {G1,G2,B3,D4};
			CPhrase bar92 = new CPhrase(0.0);
			bar92.addChord(pitchArray92a, C);

	 		int[] pitchArray92b = {G4};
			bar92.addChord(pitchArray92b, Q);

	 		int[] pitchArray92c = {B4};
			bar92.addChord(pitchArray92c, Q);
			
			int[] pitchArray92d = {G2,D4};
			bar92.addChord(pitchArray92d, C);

			
			//bar93
			CPhrase bar93 = new CPhrase(0.0);
			bar93 = bar8.copy();	
			
				
			//bar94
			CPhrase bar94 = new CPhrase(0.0);
			bar94 = bar5.copy();
				
									
			//bar95
	 		int[] pitchArray95a = {C2,E2,G4};
			CPhrase bar95 = new CPhrase(0.0);
			bar95.addChord(pitchArray95a, C);

	 		int[] pitchArray95b = {E4};
			bar95.addChord(pitchArray95b, C);

	 		int[] pitchArray95c = {C4};
			bar95.addChord(pitchArray95c, C);


			//bar96
	 		int[] pitchArray96a = {C2,E4};
			CPhrase bar96 = new CPhrase(0.0);
			bar96.addChord(pitchArray96a, C);

	 		int[] pitchArray96b = {C4};
			bar96.addChord(pitchArray96b, C);

	 		int[] pitchArray96c = {G3};
			bar96.addChord(pitchArray96c, C);
			
	
			//bar97
	 		int[] pitchArray97a = {B1,D2,G4};
			CPhrase bar97 = new CPhrase(0.0);
			bar97.addChord(pitchArray97a, Q);

	 		int[] pitchArray97b = {FS4};
			bar97.addChord(pitchArray97b, Q);

	 		int[] pitchArray97c = {G4};
			bar97.addChord(pitchArray97c, Q);
			
			int[] pitchArray97d = {D4};
			bar97.addChord(pitchArray97d, Q);

	 		int[] pitchArray97e = {B1,G2,B3};
			bar97.addChord(pitchArray97e, Q);
			
			int[] pitchArray97f = {G3};
			bar97.addChord(pitchArray97f, Q);

		
			//bar98
	 		int[] pitchArray98a = {C2,E2,C4};
			CPhrase bar98 = new CPhrase(0.0);
			bar98.addChord(pitchArray98a, Q);

	 		int[] pitchArray98b = {G2};
			bar98.addChord(pitchArray98b, Q);

	 		int[] pitchArray98c = {C2,E2,G3};
			bar98.addChord(pitchArray98c, Q);
			
			int[] pitchArray98d = {G2};
			bar98.addChord(pitchArray98d, Q);

	 		int[] pitchArray98e = {C2,E2,E4};
			bar98.addChord(pitchArray98e, Q);
			
			int[] pitchArray98f = {G2};
			bar98.addChord(pitchArray98f, Q);


			//bar99
	 		int[] pitchArray99a = {C2,A2,FS4};
			CPhrase bar99 = new CPhrase(0.0);
			bar99.addChord(pitchArray99a, C);

	 		int[] pitchArray99b = {A4};
			bar99.addChord(pitchArray99b, C);

	 		int[] pitchArray99c = {C2,A2,D4};
			bar99.addChord(pitchArray99c, C);


			//bar100
			CPhrase bar100 = new CPhrase(0.0);
			bar100 = bar5.copy();
			

			//bar101
	 		int[] pitchArray101a = {C2,G2,E4};
			CPhrase bar101 = new CPhrase(0.0);
			bar101.addChord(pitchArray101a, Q);

	 		int[] pitchArray101b = {D4};
			bar101.addChord(pitchArray101b, Q);

	 		int[] pitchArray101c = {E4};
			bar101.addChord(pitchArray101c, Q);
			
			int[] pitchArray101d = {G4};
			bar101.addChord(pitchArray101d, Q);

	 		int[] pitchArray101e = {C2,E2,C5};
			bar101.addChord(pitchArray101e, Q);
			
			int[] pitchArray101f = {G4};
			bar101.addChord(pitchArray101f, Q);


			//bar102
	 		int[] pitchArray102a = {C2,A2,FS4};
			CPhrase bar102 = new CPhrase(0.0);
			bar102.addChord(pitchArray102a, Q);

	 		int[] pitchArray102b = {D4};
			bar102.addChord(pitchArray102b, Q);

	 		int[] pitchArray102c = {A3};
			bar102.addChord(pitchArray102c, C);
			
			int[] pitchArray102d = {C2,A2,FS4};
			bar102.addChord(pitchArray102d, C);


			//bar103
	 		int[] pitchArray103a = {E2,G2,C4};
			CPhrase bar103 = new CPhrase(0.0);
			bar103.addChord(pitchArray103a, Q);

	 		int[] pitchArray103b = {E4};
			bar103.addChord(pitchArray103b, Q);

	 		int[] pitchArray103c = {C4};
			bar103.addChord(pitchArray103c, Q);
			
			int[] pitchArray103d = {G3};
			bar103.addChord(pitchArray103d, Q);

	 		int[] pitchArray103e = {E3};
			bar103.addChord(pitchArray103e, C);

						
			//bar104
	 		int[] pitchArray104a = {C2,E4};
			CPhrase bar104 = new CPhrase(0.0);
			bar104.addChord(pitchArray104a, Q);

	 		int[] pitchArray104b = {D4};
			bar104.addChord(pitchArray104b, Q);

	 		int[] pitchArray104c = {E4};
			bar104.addChord(pitchArray104c, Q);
			
			int[] pitchArray104d = {G4};
			bar104.addChord(pitchArray104d, Q);

	 		int[] pitchArray104e = {C5};
			bar104.addChord(pitchArray104e, Q);
			
			int[] pitchArray104f = {G4};
			bar104.addChord(pitchArray104f, Q);
	

			//bar105
	 		int[] pitchArray105a = {C2,FS4};
			CPhrase bar105 = new CPhrase(0.0);
			bar105.addChord(pitchArray105a, C);

	 		int[] pitchArray105b = {A4};
			bar105.addChord(pitchArray105b, Q);

	 		int[] pitchArray105c = {FS4};
			bar105.addChord(pitchArray105c, Q);
			
			int[] pitchArray105d = {D4};
			bar105.addChord(pitchArray105d, Q);

	 		int[] pitchArray105e = {FS4};
			bar105.addChord(pitchArray105e, Q);
			

			//bar106
	 		int[] pitchArray106a = {C2,A3};
			CPhrase bar106 = new CPhrase(0.0);
			bar106.addChord(pitchArray106a, C);

	 		int[] pitchArray106b = {D2,D4};
			bar106.addChord(pitchArray106b, Q);

	 		int[] pitchArray106c = {C4};
			bar106.addChord(pitchArray106c, Q);
			
			int[] pitchArray106d = {D1,B3};
			bar106.addChord(pitchArray106d, Q);

	 		int[] pitchArray106e = {A3};
			bar106.addChord(pitchArray106e, Q);
			
	
			//bar107
			CPhrase bar107 = new CPhrase(0.0);
			bar107 = bar5.copy();
			

			//bar108
	 		int[] pitchArray108a = {C2,G2,E4};
			CPhrase bar108 = new CPhrase(0.0);
			bar108.addChord(pitchArray108a, Q);

	 		int[] pitchArray108b = {G4};
			bar108.addChord(pitchArray108b, Q);

	 		int[] pitchArray108c = {C2,E2,C5};
			bar108.addChord(pitchArray108c, Q);


			//bar109
	 		int[] pitchArray109a = {F2,A2,D4};
			CPhrase bar109 = new CPhrase(0.0);
			bar109.addChord(pitchArray109a, Q);

	 		int[] pitchArray109b = {F4};
			bar109.addChord(pitchArray109b, Q);

	 		int[] pitchArray109c = {D4};
			bar109.addChord(pitchArray109c, Q);
			
			int[] pitchArray109d = {F4};
			bar109.addChord(pitchArray109d, Q);

	 		int[] pitchArray109e = {G2,D3,B3};
			bar109.addChord(pitchArray109e, Q);
			
			int[] pitchArray109f = {D4};
			bar109.addChord(pitchArray109f, Q);
			
						
			//bar110
			int[] pitchArray110a = {C2,B3,D4};
			CPhrase bar110 = new CPhrase(0.0);
			bar110.addChord(pitchArray110a, Q);

	 		int[] pitchArray110b = {A3,C4};
			bar110.addChord(pitchArray110b, Q);

	 		int[] pitchArray110c = {D2,A3,C4};
			bar110.addChord(pitchArray110c, Q);
			
			int[] pitchArray110d = {G3,B3};
			bar110.addChord(pitchArray110d, Q);

	 		int[] pitchArray110e = {D1,G3,B3};
			bar110.addChord(pitchArray110e, Q);
			
			int[] pitchArray110f = {FS3,A3};
			bar110.addChord(pitchArray110f, Q);


			//bar111
			CPhrase bar111 = new CPhrase(0.0);
			bar111 = bar8.copy();	
			
			
			//bar112
			CPhrase bar112 = new CPhrase(0.0);
			bar112 = bar49.copy();


			//bar113
	 		int[] pitchArray113a = {G2,B2,F4};
			CPhrase bar113 = new CPhrase(0.0);
			bar113.addChord(pitchArray113a, C);

	 		int[] pitchArray113b = {D4};
			bar113.addChord(pitchArray113b, C);

	 		int[] pitchArray113c = {B3};
			bar113.addChord(pitchArray113c, C);


			//bar114
	 		int[] pitchArray114a = {G2,B3,D4};
			CPhrase bar114 = new CPhrase(0.0);
			bar114.addChord(pitchArray114a, C);

	 		int[] pitchArray114b = {G2,B3,D4};
			bar114.addChord(pitchArray114b, C);

	 		int[] pitchArray114c = {G2,B3,D4};
			bar114.addChord(pitchArray114c, C);
			

			//bar115
	 		int[] pitchArray115a = {C2,E2,C4};
			CPhrase bar115 = new CPhrase(0.0);
			bar115.addChord(pitchArray115a, Q);

	 		int[] pitchArray115b = {G3};
			bar115.addChord(pitchArray115b, Q);

	 		int[] pitchArray115c = {E4};
			bar115.addChord(pitchArray115c, Q);
			
			int[] pitchArray115d = {C4};
			bar115.addChord(pitchArray115d, Q);

	 		int[] pitchArray115e = {G4};
			bar115.addChord(pitchArray115e, Q);
			
			int[] pitchArray115f = {E4};
			bar115.addChord(pitchArray115f, Q);

			
			//bar116
	 		int[] pitchArray116a = {F2,D4};
			CPhrase bar116 = new CPhrase(0.0);
			bar116.addChord(pitchArray116a, Q);

	 		int[] pitchArray116b = {F4};
			bar116.addChord(pitchArray116b, Q);

	 		int[] pitchArray116c = {A4};
			bar116.addChord(pitchArray116c, Q);
			
			int[] pitchArray116d = {F4};
			bar116.addChord(pitchArray116d, Q);

	 		int[] pitchArray116e = {G2,D4};
			bar116.addChord(pitchArray116e, Q);
			
			int[] pitchArray116f = {B3};
			bar116.addChord(pitchArray116f, Q);


			//bar117
	 		int[] pitchArray117a = {D2,FS2,D4};
			CPhrase bar117 = new CPhrase(0.0);
			bar117.addChord(pitchArray117a, Q);

	 		int[] pitchArray117b = {A3};
			bar117.addChord(pitchArray117b, Q);

	 		int[] pitchArray117c = {D4};
			bar117.addChord(pitchArray117c, Q);
			
			int[] pitchArray117d = {FS4};
			bar117.addChord(pitchArray117d, Q);

	 		int[] pitchArray117e = {A4};
			bar117.addChord(pitchArray117e, Q);
			
			int[] pitchArray117f = {FS4};
			bar117.addChord(pitchArray117f, Q);

					
			//bar118
	 		int[] pitchArray118a = {C1,E4};
			CPhrase bar118 = new CPhrase(0.0);
			bar118.addChord(pitchArray118a, Q);

	 		int[] pitchArray118b = {A4};
			bar118.addChord(pitchArray118b, Q);

	 		int[] pitchArray118c = {D2,G4};
			bar118.addChord(pitchArray118c, Q);
			
			int[] pitchArray118d = {B4};
			bar118.addChord(pitchArray118d, Q);

	 		int[] pitchArray118e = {D1,FS4};
			bar118.addChord(pitchArray118e, Q);
			
			int[] pitchArray118f = {A4};
			bar118.addChord(pitchArray118f, Q);
	
					
			//bar119
	 		int[] pitchArray119a = {C2,E2,E4};
			CPhrase bar119 = new CPhrase(0.0);
			bar119.addChord(pitchArray119a, Q);

	 		int[] pitchArray119b = {C4};
			bar119.addChord(pitchArray119b, Q);

	 		int[] pitchArray119c = {G4};
			bar119.addChord(pitchArray119c, Q);
			
			int[] pitchArray119d = {E4};
			bar119.addChord(pitchArray119d, Q);

	 		int[] pitchArray119e = {C5};
			bar119.addChord(pitchArray119e, Q);
			
			int[] pitchArray119f = {g5};
			bar119.addChord(pitchArray119f, Q);


			//bar120
	 		int[] pitchArray120a = {D2,FS2,D5};
			CPhrase bar120 = new CPhrase(0.0);
			bar120.addChord(pitchArray120a, C);

	 		int[] pitchArray120b = {A4};
			bar120.addChord(pitchArray120b, Q);

	 		int[] pitchArray120c = {FS4};
			bar120.addChord(pitchArray120c, Q);
			
			int[] pitchArray120d = {C2,FS2,D4};
			bar120.addChord(pitchArray120d, Q);

	 		int[] pitchArray120e = {A3};
			bar120.addChord(pitchArray120e, Q);


			//bar121
	 		int[] pitchArray121a = {B1,G2,G4};
			CPhrase bar121 = new CPhrase(0.0);
			bar121.addChord(pitchArray121a, C);

	 		int[] pitchArray121b = {B4};
			bar121.addChord(pitchArray121b, Q);

	 		int[] pitchArray121c = {G4};
			bar121.addChord(pitchArray121c, Q);
			
			int[] pitchArray121d = {D4};
			bar121.addChord(pitchArray121d, C);


			//bar122
	 		int[] pitchArray122a = {B1,D2,G4};
			CPhrase bar122 = new CPhrase(0.0);
			bar122.addChord(pitchArray122a, Q);

	 		int[] pitchArray122b = {FS4};
			bar122.addChord(pitchArray122b, Q);

	 		int[] pitchArray122c = {B1,D2,G4};
			bar122.addChord(pitchArray122c, Q);
			
			int[] pitchArray122d = {B4};
			bar122.addChord(pitchArray122d, Q);

	 		int[] pitchArray122e = {B1,D2,D4};
			bar122.addChord(pitchArray122e, C);
			

			//bar123
			CPhrase bar123 = new CPhrase(0.0);
			bar123 = bar5.copy();
			

			//bar124
			CPhrase bar124 = new CPhrase(0.0);
			bar124 = bar12.copy();


			//bar125
	 		int[] pitchArray125a = {G2,G4};
			CPhrase bar125 = new CPhrase(0.0);
			bar125.addChord(pitchArray125a, Q);

	 		int[] pitchArray125b = {E4};
			bar125.addChord(pitchArray125b, Q);

	 		int[] pitchArray125c = {G1,D4};
			bar125.addChord(pitchArray125c, Q);
			
			int[] pitchArray125d = {B3};
			bar125.addChord(pitchArray125d, Q);

	 		int[] pitchArray125e = {G3};
			bar125.addChord(pitchArray125e, C);


			//bar126
	 		int[] pitchArray126a = {E2,C4};
			CPhrase bar126 = new CPhrase(0.0);
			bar126.addChord(pitchArray126a, Q);

	 		int[] pitchArray126b = {G3};
			bar126.addChord(pitchArray126b, Q);

	 		int[] pitchArray126c = {C4};
			bar126.addChord(pitchArray126c, Q);
			
			int[] pitchArray126d = {E4};
			bar126.addChord(pitchArray126d, Q);

	 		int[] pitchArray126e = {E2,G4};
			bar126.addChord(pitchArray126e, Q);
			
			int[] pitchArray126f = {C2,C4,E4};
			bar126.addChord(pitchArray126f, Q);

			
			//bar127
			CPhrase bar127 = new CPhrase(0.0);
			bar127 = bar5.copy();
			

			//bar128
	 		int[] pitchArray128a = {G1,B3};
			CPhrase bar128 = new CPhrase(0.0);
			bar128.addChord(pitchArray128a, C);

	 		int[] pitchArray128b = {D4};
			bar128.addChord(pitchArray128b, C);

	 		int[] pitchArray128c = {G4};
			bar128.addChord(pitchArray128c, C);

	
			//bar129
	 		int[] pitchArray129a = {B1,D2,A4};
			CPhrase bar129 = new CPhrase(0.0);
			bar129.addChord(pitchArray129a, Q);

	 		int[] pitchArray129b = {G4};
			bar129.addChord(pitchArray129b, Q);

	 		int[] pitchArray129c = {B1,D2,FS4};
			bar129.addChord(pitchArray129c, Q);
			
			int[] pitchArray129d = {G4};
			bar129.addChord(pitchArray129d, Q);

	 		int[] pitchArray129e = {B1,G2,D4};
			bar129.addChord(pitchArray129e, C);
			
		
			//bar 130
	 		int[] pitchArray130a = {C2,E3,C4};
			CPhrase bar130 = new CPhrase(0.0);
			bar130.addChord(pitchArray130a, C);
			
			int[] pitchArray130b = {C2,E3,C4};
			bar130.addChord(pitchArray130b, C);

			int[] pitchArray130c = {C2,E3,C4};
			bar130.addChord(pitchArray130c, C);
			

			//bar131
			CPhrase bar131 = new CPhrase(0.0);
			bar131 = bar8.copy();	
			

			//bar132
	 		int[] pitchArray132a = {G2,C4,E4};
			CPhrase bar132 = new CPhrase(0.0);
			bar132.addChord(pitchArray132a, C);

	 		int[] pitchArray132b = {G1,B3,D4};
			bar132.addChord(pitchArray132b, Q);

	 		int[] pitchArray132c = {G3,B3};
			bar132.addChord(pitchArray132c, Q);
			
			int[] pitchArray132d = {G3};
			bar132.addChord(pitchArray132d, C);

			
			//bar133
	 		int[] pitchArray133a = {B1,G2,D4};
			CPhrase bar133 = new CPhrase(0.0);
			bar133.addChord(pitchArray133a, C);

	 		int[] pitchArray133b = {G4};
			bar133.addChord(pitchArray133b, Q);

	 		int[] pitchArray133c = {D4};
			bar133.addChord(pitchArray133c, Q);
			
			int[] pitchArray133d = {B3};
			bar133.addChord(pitchArray133d, Q);

	 		int[] pitchArray133e = {D4};
			bar133.addChord(pitchArray133e, Q);

			
			//bar134
			int[] pitchArray134a = {C2,A3};
			CPhrase bar134 = new CPhrase(0.0);
			bar134.addChord(pitchArray134a, Q);

	 		int[] pitchArray134b = {E4};
			bar134.addChord(pitchArray134b, Q);

	 		int[] pitchArray134c = {D2,B3,D4};
			bar134.addChord(pitchArray134c, Q);
			
			int[] pitchArray134d = {A3,C4};
			bar134.addChord(pitchArray134d, Q);

	 		int[] pitchArray134e = {D1,G3,B3};
			bar134.addChord(pitchArray134e, Q);
			
			int[] pitchArray134f = {FS3,A3};
			bar134.addChord(pitchArray134f, Q);
			

			//bar135
	 		int[] pitchArray135a = {C2,D2,FS4};
			CPhrase bar135 = new CPhrase(0.0);
			bar135.addChord(pitchArray135a, C);

	 		int[] pitchArray135b = {C2,D2,FS4};
			bar135.addChord(pitchArray135b, Q);

	 		int[] pitchArray135c = {D4};
			bar135.addChord(pitchArray135c, Q);
			
			int[] pitchArray135d = {C2,D2,A4};
			bar135.addChord(pitchArray135d, C);


			//bar136
	 		int[] pitchArray136a = {C2,E2,C5};
			CPhrase bar136 = new CPhrase(0.0);
			bar136.addChord(pitchArray136a, Q);

	 		int[] pitchArray136b = {B4};
			bar136.addChord(pitchArray136b, Q);

	 		int[] pitchArray136c = {C5};
			bar136.addChord(pitchArray136c, Q);
			
			int[] pitchArray136d = {G4};
			bar136.addChord(pitchArray136d, Q);

	 		int[] pitchArray136e = {E4};
			bar136.addChord(pitchArray136e, Q);
			
			int[] pitchArray136f = {C4};
			bar136.addChord(pitchArray136f, Q);


			//bar137
	 		int[] pitchArray137a = {C2,E2,C4};
			CPhrase bar137 = new CPhrase(0.0);
			bar137.addChord(pitchArray137a, Q);

	 		int[] pitchArray137b = {G2};
			bar137.addChord(pitchArray137b, Q);

	 		int[] pitchArray137c = {C2,E2,G3};
			bar137.addChord(pitchArray137c, Q);
			
			int[] pitchArray137d = {G2};
			bar137.addChord(pitchArray137d, Q);

	 		int[] pitchArray137e = {C2,E2,E4};
			bar137.addChord(pitchArray137e, Q);
			
			int[] pitchArray137f = {G2};
			bar137.addChord(pitchArray137f, Q);


			//bar138
	 		int[] pitchArray138a = {D1,A3,D4,FS4};
			CPhrase bar138 = new CPhrase(0.0);
			bar138.addChord(pitchArray138a, Q);

	 		int[] pitchArray138b = {D2};
			bar138.addChord(pitchArray138b, Q);

	 		int[] pitchArray138c = {cs2,FS4};
			bar138.addChord(pitchArray138c, Q);
			
			int[] pitchArray138d = {D2};
			bar138.addChord(pitchArray138d, Q);

	 		int[] pitchArray138e = {C2};
			bar138.addChord(pitchArray138e, Q);
			
			int[] pitchArray138f = {D2};
			bar138.addChord(pitchArray138f, Q);


			//bar139
	 		int[] pitchArray139a = {B1,G4};
			CPhrase bar139 = new CPhrase(0.0);
			bar139.addChord(pitchArray139a, Q);

	 		int[] pitchArray139b = {B4};
			bar139.addChord(pitchArray139b, Q);

	 		int[] pitchArray139c = {G4};
			bar139.addChord(pitchArray139c, Q);
			
			int[] pitchArray139d = {B4};
			bar139.addChord(pitchArray139d, Q);

	 		int[] pitchArray139e = {D4};
			bar139.addChord(pitchArray139e, C);


			//bar140
	 		int[] pitchArray140a = {C2,FS2,A3};
			CPhrase bar140 = new CPhrase(0.0);
			bar140.addChord(pitchArray140a, C);

	 		int[] pitchArray140b = {C2,FS2,A3};
			bar140.addChord(pitchArray140b, Q);

	 		int[] pitchArray140c = {D4};
			bar140.addChord(pitchArray140c, Q);
			
			int[] pitchArray140d = {C2,A2,FS4};
			bar140.addChord(pitchArray140d, C);


			//bar141
	 		int[] pitchArray141a = {B1,G2,D4};
			CPhrase bar141 = new CPhrase(0.0);
			bar141.addChord(pitchArray141a, Q);

	 		int[] pitchArray141b = {E4};
			bar141.addChord(pitchArray141b, Q);

	 		int[] pitchArray141c = {F4};
			bar141.addChord(pitchArray141c, Q);
			
			int[] pitchArray141d = {D4};
			bar141.addChord(pitchArray141d, Q);

	 		int[] pitchArray141e = {G1,C4};
			bar141.addChord(pitchArray141e, Q);
			
			int[] pitchArray141f = {B3};
			bar141.addChord(pitchArray141f, Q);

			
			//bar 142
	 		int[] pitchArray142a = {C2,E2,C4};
			CPhrase bar142 = new CPhrase(0.0);
			bar142.addChord(pitchArray142a, C);
			
			int[] pitchArray142b = {G3};
			bar142.addChord(pitchArray142b, C);

			int[] pitchArray142c = {E4};
			bar142.addChord(pitchArray142c, C);
			

			//bar143
	 		int[] pitchArray143a = {B1,D2,G4};
			CPhrase bar143 = new CPhrase(0.0);
			bar143.addChord(pitchArray143a, C);

	 		int[] pitchArray143b = {D4};
			bar143.addChord(pitchArray143b, Q);

	 		int[] pitchArray143c = {B3};
			bar143.addChord(pitchArray143c, Q);
			
			int[] pitchArray143d = {B1,D2,G3};
			bar143.addChord(pitchArray143d, C);


			//bar144
	 		int[] pitchArray144a = {C2,E2,G4};
			CPhrase bar144 = new CPhrase(0.0);
			bar144.addChord(pitchArray144a, Q);

	 		int[] pitchArray144b = {G2};
			bar144.addChord(pitchArray144b, Q);

	 		int[] pitchArray144c = {C2,E2,C4};
			bar144.addChord(pitchArray144c, Q);
			
			int[] pitchArray144d = {G2};
			bar144.addChord(pitchArray144d, Q);

	 		int[] pitchArray144e = {C2,E2,E4};
			bar144.addChord(pitchArray144e, Q);
			
			int[] pitchArray144f = {G2};
			bar144.addChord(pitchArray144f, Q);

			
			//bar145
	 		int[] pitchArray145a = {F2,D4};
			CPhrase bar145 = new CPhrase(0.0);
			bar145.addChord(pitchArray145a, Q);

	 		int[] pitchArray145b = {F4};
			bar145.addChord(pitchArray145b, Q);

	 		int[] pitchArray145c = {A3};
			bar145.addChord(pitchArray145c, Q);
			
			int[] pitchArray145d = {D4};
			bar145.addChord(pitchArray145d, Q);

	 		int[] pitchArray145e = {G2,B3};
			bar145.addChord(pitchArray145e, Q);
			
			int[] pitchArray145f = {D4};
			bar145.addChord(pitchArray145f, Q);
			
			
			//bar146
	 		int[] pitchArray146a = {C2,FS3,D4};
			CPhrase bar146 = new CPhrase(0.0);
			bar146.addChord(pitchArray146a, C);

	 		int[] pitchArray146b = {C2,D4,FS4};
			bar146.addChord(pitchArray146b, C);

	 		int[] pitchArray146c = {C2,FS4,A4};
			bar146.addChord(pitchArray146c, C);

	
			//bar147
	 		int[] pitchArray147a = {C2,E4};
			CPhrase bar147 = new CPhrase(0.0);
			bar147.addChord(pitchArray147a, Q);

	 		int[] pitchArray147b = {C5};
			bar147.addChord(pitchArray147b, Q);

	 		int[] pitchArray147c = {D2,B4};
			bar147.addChord(pitchArray147c, Q);
			
			int[] pitchArray147d = {G4};
			bar147.addChord(pitchArray147d, Q);

	 		int[] pitchArray147e = {D1,A4};
			bar147.addChord(pitchArray147e, Q);
			
			int[] pitchArray147f = {FS4};
			bar147.addChord(pitchArray147f, Q);
		
		
			//bar148
	 		int[] pitchArray148a = {C2,E2,C5};
			CPhrase bar148 = new CPhrase(0.0);
			bar148.addChord(pitchArray148a, Q);

	 		int[] pitchArray148b = {B4};
			bar148.addChord(pitchArray148b, Q);

	 		int[] pitchArray148c = {C5};
			bar148.addChord(pitchArray148c, Q);
			
			int[] pitchArray148d = {G4};
			bar148.addChord(pitchArray148d, Q);

	 		int[] pitchArray148e = {E4};
			bar148.addChord(pitchArray148e, Q);
			
			int[] pitchArray148f = {C4};
			bar148.addChord(pitchArray148f, Q);


			//bar149
	 		int[] pitchArray149a = {F2,F4};
			CPhrase bar149 = new CPhrase(0.0);
			bar149.addChord(pitchArray149a, Q);

	 		int[] pitchArray149b = {D4};
			bar149.addChord(pitchArray149b, Q);

	 		int[] pitchArray149c = {A3};
			bar149.addChord(pitchArray149c, Q);
			
			int[] pitchArray149d = {G2,B3};
			bar149.addChord(pitchArray149d, Q);


			//bar150
	 		int[] pitchArray150a = {C2,G3,C4,E4};
			CPhrase bar150 = new CPhrase(0.0);
			bar150.addChord(pitchArray150a, Q);

	 		int[] pitchArray150b = {B1};
			bar150.addChord(pitchArray150b, Q);

	 		int[] pitchArray150c = {C2,E4};
			bar150.addChord(pitchArray150c, Q);
			
			int[] pitchArray150d = {D2};
			bar150.addChord(pitchArray150d, Q);

	 		int[] pitchArray150e = {E2};
			bar150.addChord(pitchArray150e, Q);
			
			int[] pitchArray150f = {FS2};
			bar150.addChord(pitchArray150f, Q);

			
			//bar151
			CPhrase bar151 = new CPhrase(0.0);
			bar151 = bar8.copy();	
			
			
			//bar152
	 		int[] pitchArray152a = {C2,E2,G4};
			CPhrase bar152 = new CPhrase(0.0);
			bar152.addChord(pitchArray152a, C);

	 		int[] pitchArray152b = {F4};
			bar152.addChord(pitchArray152b, Q);

	 		int[] pitchArray152c = {E4};
			bar152.addChord(pitchArray152c, Q);
			
			int[] pitchArray152d = {D4};
			bar152.addChord(pitchArray152d, Q);

	 		int[] pitchArray152e = {C4};
			bar152.addChord(pitchArray152e, Q);
			

			//bar154
	 		int[] pitchArray154a = {C2,D4};
			CPhrase bar154 = new CPhrase(0.0);
			bar154.addChord(pitchArray154a, Q);

	 		int[] pitchArray154b = {cs4};
			bar154.addChord(pitchArray154b, Q);

	 		int[] pitchArray154c = {D4};
			bar154.addChord(pitchArray154c, Q);
			
			int[] pitchArray154d = {FS4};
			bar154.addChord(pitchArray154d, Q);

	 		int[] pitchArray154e = {A4};
			bar154.addChord(pitchArray154e, Q);
			
			int[] pitchArray154f = {FS4};
			bar154.addChord(pitchArray154f, Q);


			//bar155
	 		int[] pitchArray155a = {B1,D2,G4};
			CPhrase bar155 = new CPhrase(0.0);
			bar155.addChord(pitchArray155a, Q);

	 		int[] pitchArray155b = {B4};
			bar155.addChord(pitchArray155b, Q);

	 		int[] pitchArray155c = {G4};
			bar155.addChord(pitchArray155c, Q);
			
			int[] pitchArray155d = {D4};
			bar155.addChord(pitchArray155d, Q);

	 		int[] pitchArray155e = {B3};
			bar155.addChord(pitchArray155e, Q);
			
			int[] pitchArray155f = {G3};
			bar155.addChord(pitchArray155f, Q);


			//bar156
	 		int[] pitchArray156a = {E2,G2,C4};
			CPhrase bar156 = new CPhrase(0.0);
			bar156.addChord(pitchArray156a, Q);

	 		int[] pitchArray156b = {G3};
			bar156.addChord(pitchArray156b, Q);

	 		int[] pitchArray156c = {E4};
			bar156.addChord(pitchArray156c, Q);
			
			int[] pitchArray156d = {C4};
			bar156.addChord(pitchArray156d, Q);

	 		int[] pitchArray156e = {G4};
			bar156.addChord(pitchArray156e, C);
			
			
			//bar157
	 		int[] pitchArray157a = {C2,E4};
			CPhrase bar157 = new CPhrase(0.0);
			bar157.addChord(pitchArray157a, Q);

	 		int[] pitchArray157b = {D4};
			bar157.addChord(pitchArray157b, Q);

	 		int[] pitchArray157c = {E4};
			bar157.addChord(pitchArray157c, Q);
			
			int[] pitchArray157d = {G4};
			bar157.addChord(pitchArray157d, Q);

	 		int[] pitchArray157e = {C5};
			bar157.addChord(pitchArray157e, Q);
			
			int[] pitchArray157f = {G4};
			bar157.addChord(pitchArray157f, Q);


			//bar158
	 		int[] pitchArray158a = {G1,B3};
			CPhrase bar158 = new CPhrase(0.0);
			bar158.addChord(pitchArray158a, C);

	 		int[] pitchArray158b = {D4};
			bar158.addChord(pitchArray158b, Q);

	 		int[] pitchArray158c = {B3};
			bar158.addChord(pitchArray158c, Q);
			
			int[] pitchArray158d = {A3};
			bar158.addChord(pitchArray158d, Q);

	 		int[] pitchArray158e = {G3};
			bar158.addChord(pitchArray158e, Q);


			//bar159
	 		int[] pitchArray159a = {C2,E4};
			CPhrase bar159 = new CPhrase(0.0);
			bar159.addChord(pitchArray159a, Q);

	 		int[] pitchArray159b = {G4};
			bar159.addChord(pitchArray159b, Q);

	 		int[] pitchArray159c = {D2,D4};
			bar159.addChord(pitchArray159c, Q);
			
			int[] pitchArray159d = {C4};
			bar159.addChord(pitchArray159d, Q);

	 		int[] pitchArray159e = {D1,B3};
			bar159.addChord(pitchArray159e, Q);
			
			int[] pitchArray159f = {A3};
			bar159.addChord(pitchArray159f, Q);


			//bar160
	 		int[] pitchArray160a = {C2,E2,C4};
			CPhrase bar160 = new CPhrase(0.0);
			bar160.addChord(pitchArray160a, Q);

	 		int[] pitchArray160b = {B3};
			bar160.addChord(pitchArray160b, Q);

	 		int[] pitchArray160c = {C4};
			bar160.addChord(pitchArray160c, Q);
			
			int[] pitchArray160d = {E4};
			bar160.addChord(pitchArray160d, Q);

	 		int[] pitchArray160e = {C2,E2,G3};
			bar160.addChord(pitchArray160e, Q);
			
			int[] pitchArray160f = {C4};
			bar160.addChord(pitchArray160f, Q);


			//bar161
	 		int[] pitchArray161a = {C2,FS3,D4};
			CPhrase bar161 = new CPhrase(0.0);
			bar161.addChord(pitchArray161a, C);

	 		int[] pitchArray161b = {C2,FS3,D4};
			bar161.addChord(pitchArray161b, C);

	 		int[] pitchArray161c = {C2,FS3,D4};
			bar161.addChord(pitchArray161c, C);
			

			//bar162
	 		int[] pitchArray162a = {C2,G2,E4};
			CPhrase bar162 = new CPhrase(0.0);
			bar162.addChord(pitchArray162a, Q);

	 		int[] pitchArray162b = {D4};
			bar162.addChord(pitchArray162b, Q);

	 		int[] pitchArray162c = {E4};
			bar162.addChord(pitchArray162c, Q);
			
			int[] pitchArray162d = {G4};
			bar162.addChord(pitchArray162d, Q);

	 		int[] pitchArray162e = {C2,E2,C5};
			bar162.addChord(pitchArray162e, Q);
			
			int[] pitchArray162f = {G4};
			bar162.addChord(pitchArray162f, Q);


			//bar163
	 		int[] pitchArray163a = {B1,D2,G4};
			CPhrase bar163 = new CPhrase(0.0);
			bar163.addChord(pitchArray163a, Q);

	 		int[] pitchArray163b = {FS4};
			bar163.addChord(pitchArray163b, Q);

	 		int[] pitchArray163c = {G4};
			bar163.addChord(pitchArray163c, Q);
			
			int[] pitchArray163d = {D4};
			bar163.addChord(pitchArray163d, Q);

	 		int[] pitchArray163e = {B3};
			bar163.addChord(pitchArray163e, Q);
			
			int[] pitchArray163f = {G3};
			bar163.addChord(pitchArray163f, Q);


			//bar164
	 		int[] pitchArray164a = {G2,D4};
			CPhrase bar164 = new CPhrase(0.0);
			bar164.addChord(pitchArray164a, Q);

	 		int[] pitchArray164b = {FS2};
			bar164.addChord(pitchArray164b, Q);

	 		int[] pitchArray164c = {G2,G3};
			bar164.addChord(pitchArray164c, Q);
			
			int[] pitchArray164d = {D2};
			bar164.addChord(pitchArray164d, Q);

	 		int[] pitchArray164e = {B1};
			bar164.addChord(pitchArray164e, Q);
			
			int[] pitchArray164f = {G1};
			bar164.addChord(pitchArray164f, Q);


			//bar165
	 		int[] pitchArray165a = {B1,D4};
			CPhrase bar165 = new CPhrase(0.0);
			bar165.addChord(pitchArray165a, C);

	 		int[] pitchArray165b = {B3};
			bar165.addChord(pitchArray165b, C);

	 		int[] pitchArray165c = {G3};
			bar165.addChord(pitchArray165c, C);


			//bar166
	 		int[] pitchArray166a = {G2,B2,D4};
			CPhrase bar166 = new CPhrase(0.0);
			bar166.addChord(pitchArray166a, Q);

	 		int[] pitchArray166b = {B4};
			bar166.addChord(pitchArray166b, Q);

	 		int[] pitchArray166c = {G4};
			bar166.addChord(pitchArray166c, Q);
			
			int[] pitchArray166d = {D4};
			bar166.addChord(pitchArray166d, Q);

	 		int[] pitchArray166e = {B3};
			bar166.addChord(pitchArray166e, C);


			//bar167
	 		int[] pitchArray167a = {C2,E2,C4};
			CPhrase bar167 = new CPhrase(0.0);
			bar167.addChord(pitchArray167a, C);

	 		int[] pitchArray167b = {C4};
			bar167.addChord(pitchArray167b, Q);

	 		int[] pitchArray167c = {D4};
			bar167.addChord(pitchArray167c, Q);
			
			int[] pitchArray167d = {E4};
			bar167.addChord(pitchArray167d, C);


			//bar168
	 		int[] pitchArray168a = {C2,E2,G4};
			CPhrase bar168 = new CPhrase(0.0);
			bar168.addChord(pitchArray168a, C);

	 		int[] pitchArray168b = {F4};
			bar168.addChord(pitchArray168b, Q);

	 		int[] pitchArray168c = {E4};
			bar168.addChord(pitchArray168c, Q);
			
			int[] pitchArray168d = {E2,G2,D4};
			bar168.addChord(pitchArray168d, Q);

	 		int[] pitchArray168e = {C4};
			bar168.addChord(pitchArray168e, Q);


			//bar169
	 		int[] pitchArray169a = {C2,E4};
			CPhrase bar169 = new CPhrase(0.0);
			bar169.addChord(pitchArray169a, Q);

	 		int[] pitchArray169b = {G4};
			bar169.addChord(pitchArray169b, Q);

	 		int[] pitchArray169c = {D2,D4};
			bar169.addChord(pitchArray169c, Q);
			
			int[] pitchArray169d = {G4};
			bar169.addChord(pitchArray169d, Q);

	 		int[] pitchArray169e = {D1,A3};
			bar169.addChord(pitchArray169e, Q);
			
			int[] pitchArray169f = {FS4};
			bar169.addChord(pitchArray169f, Q);


			//bar170
			CPhrase bar170 = new CPhrase(0.0);
			bar170 = bar8.copy();	
			
			
			//bar171
	 		int[] pitchArray171a = {G1,G2,B3};
			CPhrase bar171 = new CPhrase(0.0);
			bar171.addChord(pitchArray171a, Q);

	 		int[] pitchArray171b = {C4};
			bar171.addChord(pitchArray171b, Q);

	 		int[] pitchArray171c = {D4};
			bar171.addChord(pitchArray171c, Q);
			
			int[] pitchArray171d = {E4};
			bar171.addChord(pitchArray171d, Q);

	 		int[] pitchArray171e = {B1,G2,F4};
			bar171.addChord(pitchArray171e, Q);
			
			int[] pitchArray171f = {E4};
			bar171.addChord(pitchArray171f, Q);


			//bar172
			CPhrase bar172 = new CPhrase(0.0);
			bar172 = bar8.copy();	
			

			//bar173
	 		int[] pitchArray173a = {F2,F4};
			CPhrase bar173 = new CPhrase(0.0);
			bar173.addChord(pitchArray173a, Q);

	 		int[] pitchArray173b = {A4};
			bar173.addChord(pitchArray173b, Q);

	 		int[] pitchArray173c = {A3};
			bar173.addChord(pitchArray173c, C);
			
			int[] pitchArray173d = {G2,B3};
			bar173.addChord(pitchArray173d, Q);

	 		int[] pitchArray173e = {D4};
			bar173.addChord(pitchArray173e, Q);
			
						
			//bar174
	 		int[] pitchArray174a = {C2,E2,G3};
			CPhrase bar174 = new CPhrase(0.0);
			bar174.addChord(pitchArray174a, Q);

	 		int[] pitchArray174b = {G2};
			bar174.addChord(pitchArray174b, Q);

	 		int[] pitchArray174c = {C2,E2,C4};
			bar174.addChord(pitchArray174c, Q);
			
			int[] pitchArray174d = {G2};
			bar174.addChord(pitchArray174d, Q);

	 		int[] pitchArray174e = {C2,E2,E4};
			bar174.addChord(pitchArray174e, Q);
			
			int[] pitchArray174f = {G2};
			bar174.addChord(pitchArray174f, Q);


			//bar175
	 		int[] pitchArray175a = {G2,E4};
			CPhrase bar175 = new CPhrase(0.0);
			bar175.addChord(pitchArray175a, Q);

	 		int[] pitchArray175b = {C4};
			bar175.addChord(pitchArray175b, Q);

	 		int[] pitchArray175c = {G1,B3};
			bar175.addChord(pitchArray175c, Q);
			
			int[] pitchArray175d = {D4};
			bar175.addChord(pitchArray175d, Q);

	 		int[] pitchArray175e = {G4};
			bar175.addChord(pitchArray175e, C);


			//bar176
	 		int[] pitchArray176a = {B1,D2,A4};
			CPhrase bar176 = new CPhrase(0.0);
			bar176.addChord(pitchArray176a, Q);

	 		int[] pitchArray176b = {G4};
			bar176.addChord(pitchArray176b, Q);

	 		int[] pitchArray176c = {B4};
			bar176.addChord(pitchArray176c, Q);
			
			int[] pitchArray176d = {G4};
			bar176.addChord(pitchArray176d, Q);

	 		int[] pitchArray176e = {B1,D2,D4};
			bar176.addChord(pitchArray176e, Q);
			
			int[] pitchArray176f = {G4};
			bar176.addChord(pitchArray176f, Q);

			//Add the phrases to the phrase tables (two-dimensional arrays)
			//table 1 (for the first part of the waltz)
			getBarArray()[0][0] = 	bar96;
			getBarArray()[0][1] = 	bar32;
			getBarArray()[0][2] = 	bar69;
			getBarArray()[0][3] = 	bar40;	
			getBarArray()[0][4] = 	bar148;
			getBarArray()[0][5] = 	bar104;
			getBarArray()[0][6] = 	bar152;
			getBarArray()[0][7] = 	bar119;
			getBarArray()[0][8] = 	bar98;
			getBarArray()[0][9] = 	bar3;	
			getBarArray()[0][10] = bar54;
	
			getBarArray()[1][0] = 	bar22;
			getBarArray()[1][1] = 	bar6;
			getBarArray()[1][2] = 	bar95;
			getBarArray()[1][3] = 	bar17;	
			getBarArray()[1][4] = 	bar74;
			getBarArray()[1][5] = 	bar157;
			getBarArray()[1][6] = 	bar60;
			getBarArray()[1][7] = 	bar84;	
			getBarArray()[1][8] = 	bar142;
			getBarArray()[1][9] = 	bar87;
			getBarArray()[1][10] = bar130;

			getBarArray()[2][0] = 	bar141;
			getBarArray()[2][1] = 	bar128;
			getBarArray()[2][2] = 	bar158;
			getBarArray()[2][3] = 	bar113;	
			getBarArray()[2][4] = 	bar163;
			getBarArray()[2][5] = 	bar27;
			getBarArray()[2][6] = 	bar171;
			getBarArray()[2][7] = 	bar114;
			getBarArray()[2][8] = 	bar42;
			getBarArray()[2][9] = 	bar165;	
			getBarArray()[2][10] =	bar10;
	
			getBarArray()[3][0] = 	bar41;
			getBarArray()[3][1] = 	bar63;
			getBarArray()[3][2] = 	bar13;
			getBarArray()[3][3] = 	bar85;	
			getBarArray()[3][4] = 	bar45;
			getBarArray()[3][5] = 	bar167;
			getBarArray()[3][6] = 	bar53;
			getBarArray()[3][7] = 	bar50;	
			getBarArray()[3][8] = 	bar156;
			getBarArray()[3][9] = 	bar61;
			getBarArray()[3][10] =	bar103;

			getBarArray()[4][0] = 	bar105;
			getBarArray()[4][1] = 	bar146;
			getBarArray()[4][2] = 	bar158;
			getBarArray()[4][3] = 	bar161;	
			getBarArray()[4][4] = 	bar80;
			getBarArray()[4][5] = 	bar154;
			getBarArray()[4][6] = 	bar99;
			getBarArray()[4][7] = 	bar140;
			getBarArray()[4][8] = 	bar75;
			getBarArray()[4][9] = 	bar135;	
			getBarArray()[4][10] = bar28;
	
			getBarArray()[5][0] = 	bar122;
			getBarArray()[5][1] = 	bar46;
			getBarArray()[5][2] = 	bar55;
			getBarArray()[5][3] = 	bar2;	
			getBarArray()[5][4] = 	bar97;
			getBarArray()[5][5] = 	bar68;
			getBarArray()[5][6] = 	bar133;
			getBarArray()[5][7] = 	bar86;	
			getBarArray()[5][8] = 	bar129;
			getBarArray()[5][9] = 	bar47;
			getBarArray()[5][10] = bar37;
			
			getBarArray()[6][0] = 	bar11;
			getBarArray()[6][1] = 	bar134;
			getBarArray()[6][2] = 	bar110;
			getBarArray()[6][3] = 	bar159;	
			getBarArray()[6][4] = 	bar36;
			getBarArray()[6][5] = 	bar118;
			getBarArray()[6][6] = 	bar21;
			getBarArray()[6][7] = 	bar169;
			getBarArray()[6][8] = 	bar62;
			getBarArray()[6][9] = 	bar147;	
			getBarArray()[6][10] = bar106;
	
			getBarArray()[7][0] = 	bar30;
			getBarArray()[7][1] = 	bar81;
			getBarArray()[7][2] = 	bar24;
			getBarArray()[7][3] = 	bar100;	
			getBarArray()[7][4] = 	bar107;
			getBarArray()[7][5] = 	bar91;
			getBarArray()[7][6] = 	bar127;
			getBarArray()[7][7] = 	bar94;	
			getBarArray()[7][8] = 	bar123;
			getBarArray()[7][9] = 	bar33;
			getBarArray()[7][10] = bar5;
			
			
			//and table 2...
			barArray2[0][0] = 	bar70;
			barArray2[0][1] = 	bar117;
			barArray2[0][2] = 	bar66;
			barArray2[0][3] = 	bar90;	
			barArray2[0][4] = 	bar25;
			barArray2[0][5] = 	bar138;
			barArray2[0][6] = 	bar16;
			barArray2[0][7] = 	bar120;
			barArray2[0][8] = 	bar65;
			barArray2[0][9] = 	bar102;	
			barArray2[0][10] =  bar35;
	
			barArray2[1][0] = 	bar121;
			barArray2[1][1] = 	bar39;
			barArray2[1][2] = 	bar139;
			barArray2[1][3] = 	bar17;	
			barArray2[1][4] = 	bar74;
			barArray2[1][5] = 	bar157;
			barArray2[1][6] = 	bar60;
			barArray2[1][7] = 	bar84;	
			barArray2[1][8] = 	bar142;
			barArray2[1][9] = 	bar87;
			barArray2[1][10] =  bar130;

			barArray2[2][0] = 	bar26;
			barArray2[2][1] = 	bar126;
			barArray2[2][2] = 	bar15;
			barArray2[2][3] = 	bar7;	
			barArray2[2][4] = 	bar64;
			barArray2[2][5] = 	bar150;
			barArray2[2][6] = 	bar57;
			barArray2[2][7] = 	bar48;
			barArray2[2][8] = 	bar19;
			barArray2[2][9] = 	bar31;	
			barArray2[2][10] =	bar108;
	
			barArray2[3][0] = 	bar9;
			barArray2[3][1] = 	bar56;
			barArray2[3][2] = 	bar132;
			barArray2[3][3] = 	bar34;	
			barArray2[3][4] = 	bar125;
			barArray2[3][5] = 	bar29;
			barArray2[3][6] = 	bar175;
			barArray2[3][7] = 	bar166;	
			barArray2[3][8] = 	bar82;
			barArray2[3][9] = 	bar164;
			barArray2[3][10] =	bar92;

			barArray2[4][0] = 	bar112;
			barArray2[4][1] = 	bar174;
			barArray2[4][2] = 	bar73;
			barArray2[4][3] = 	bar67;	
			barArray2[4][4] = 	bar76;
			barArray2[4][5] = 	bar101;
			barArray2[4][6] = 	bar43;
			barArray2[4][7] = 	bar51;
			barArray2[4][8] = 	bar137;
			barArray2[4][9] = 	bar144;	
			barArray2[4][10] =  bar12;
	
			barArray2[5][0] = 	bar49;
			barArray2[5][1] = 	bar18;
			barArray2[5][2] = 	bar58;
			barArray2[5][3] = 	bar160;	
			barArray2[5][4] = 	bar136;
			barArray2[5][5] = 	bar162;
			barArray2[5][6] = 	bar168;
			barArray2[5][7] = 	bar115;	
			barArray2[5][8] = 	bar38;
			barArray2[5][9] = 	bar59;
			barArray2[5][10] =  bar124;
			
			barArray2[6][0] = 	bar109;
			barArray2[6][1] = 	bar116;
			barArray2[6][2] = 	bar145;
			barArray2[6][3] = 	bar52;	
			barArray2[6][4] = 	bar1;
			barArray2[6][5] = 	bar23;
			barArray2[6][6] = 	bar89;
			barArray2[6][7] = 	bar72;
			barArray2[6][8] = 	bar149;
			barArray2[6][9] = 	bar173;	
			barArray2[6][10] =  bar44;
	
			barArray2[7][0] = 	bar14;
			barArray2[7][1] = 	bar83;
			barArray2[7][2] = 	bar79;
			barArray2[7][3] = 	bar170;	
			barArray2[7][4] = 	bar93;
			barArray2[7][5] = 	bar151;
			barArray2[7][6] = 	bar172;
			barArray2[7][7] = 	bar111;	
			barArray2[7][8] = 	bar8;
			barArray2[7][9] = 	bar78;
			barArray2[7][10] =  bar131;			
	}

		public CPhrase[][] getBarArray() {
			return barArray;
		}
		
		public CPhrase[][] getBarArray2() {
			return barArray2;
		}		
}
		
