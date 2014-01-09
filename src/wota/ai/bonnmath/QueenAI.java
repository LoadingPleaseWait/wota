/**
 * 
 */
package wota.ai.bonnmath;

import wota.gamemaster.AIInformation;
import wota.gameobjects.*;
import wota.utility.SeededRandomizer;
import wota.gameobjects.Caste;


import java.util.LinkedList;
import java.util.List;

import wota.utility.Modulo;
import wota.utility.Vector;
/**
 *
 */
@AIInformation(creator = "Simon", name = "Ballmann")
public class QueenAI extends MyQueenAI {

	/*
	 * your Queen is not able to move but can
	 * communicate and create new ants. 
	 * 
	 * You can create new ants with				createAnt(caste, antAIClass)		
	 * e.g. if you want a gatherer and the AI
	 * you want use is called SuperGathererAI	createAnt(Caste.Gatherer, SuperGathererAI.class)
	 * 
	 */

	LinkedList<Haufen> sugarlist=new LinkedList<Haufen>();
	LinkedList<Haufen> gegnerlist= new LinkedList<Haufen>();
	LinkedList<Haufen> oldsugarlist= new LinkedList<Haufen>();
	int scoutlost=100000;
	int numbergatherer=0;
	int numberoftick=0;
	int phase=0;
	boolean saving=false;
	@Override
	public void tick() throws Exception {
//		for(Haufen haufen : sugarlist){
	//		System.out.println(haufen.getPosition().length());
		//}
		numberoftick++;
		if(numberoftick==8000){
			saving=true;
		}
		/* 
		 * try to create an Ant using the TemplateAI in every tick
		 * if you don't have enough food to create the ant your call
		 * will be ignored
		 */
		scoutlost++;
		int lostsouls=0;
		int lostsoldiers=0;
		for(Message message : audibleMessages){
			if(message.sender.caste == Caste.Scout){
				scoutlost=0;
			}
			if(true){	
				if(mod(message.content,10)==1){
					Haufen neuerhaufen=talktovect(message.content);
					if(vorhandencheck(neuerhaufen,oldsugarlist)==-1){
						if(vorhandencheck(neuerhaufen, sugarlist)==-1){
							sugarlist.add(insertionposition(neuerhaufen, sugarlist), neuerhaufen);
						}else{
							sugarlist.get(vorhandencheck(neuerhaufen, sugarlist)).setamount(neuerhaufen.getamount());
						}
					}
				}
				if(mod(message.content,10)==2 && message.sender.caste == Caste.Scout){
					if(gegnerlist.size()==0){
						Haufen newhaufen=talktovect(message.content);
						gegnerlist.add(newhaufen);
					}
				}
				if(mod(message.content,10)==2 && (message.sender.caste == Caste.Gatherer||message.sender.caste == Caste.Soldier)){
					if(message.sender.caste == Caste.Gatherer){
						lostsouls++;
					}
					if(message.sender.caste == Caste.Soldier){
						lostsoldiers++;
					}
					Haufen neuerhaufen=talktovect(message.content);
					if(vorhandencheck(neuerhaufen,sugarlist)!=-1){
						sugarlist.remove(vorhandencheck(neuerhaufen,sugarlist));
					}
					if(vorhandencheck(neuerhaufen,sugarlist)==-1){
						oldsugarlist.add(neuerhaufen);
					}
				}
				
			}
		}
		if(scoutlost>parameters.SIZE_X*(2*4.14)/15 && closest(visibleHills).food>parameters.ANT_COST){
			createAnt(Caste.Scout, Scholze.class);
			scoutlost=0;
		}
		
		Vector position=new Vector(0,0);
	/*	if(sugarlist.size()>0){
			if(SeededRandomizer.getDouble()<0.5){
				position=sugarlist.get(0).getPosition();
			}else{
				position=sugarlist.get(SeededRandomizer.getInt(sugarlist.size())).getPosition();
			}
			talk(((int)((Math.round(position.x+parameters.SIZE_X/2))*Math.round(parameters.SIZE_Y)*10)+((int)((Math.round(position.y+parameters.SIZE_Y/2))*10+1))));
		}*/
		int numberofcreatedants=0;
		boolean check=true;
		boolean sugarcheck=true;
		if(saving){
			if(closest(visibleHills).food>=parameters.ANT_COST*5){
				for(int i=0;i<5;i++){
					createAnt(Caste.Soldier, Teichner.class);
				}
				saving=false;
			}
		}else{
			if(check && gegnerlist.size()>0){
				gegnerlist.get(0).setants(gegnerlist.get(0).getsoldiers()+lostsoldiers);
				position=gegnerlist.get(0).getPosition();
				if(gegnerlist.get(0).getsoldiers()<2){
					check=false;
					sugarcheck=false;
				//	while(closest(visibleHills).food>parameters.ANT_COST*(1+numberofcreatedants)){
					for(int i=0; i<2 && closest(visibleHills).food>parameters.ANT_COST*(1+numberofcreatedants);i++){
						createAnt(Caste.Soldier, Rapoport.class);
						numberofcreatedants++;
						gegnerlist.get(0).setsoldiers(gegnerlist.get(0).getsoldiers()+1);
					}
				}
			}
			if(numbergatherer<10 || SeededRandomizer.getDouble()<0.9){
				for(Haufen haufen : sugarlist){	
					if(check){
						if(haufen.getsoldiers()<2){
							haufen.setsoldiers(haufen.getsoldiers()+lostsoldiers);
							check=false;
							position=haufen.getPosition();
							while(haufen.getsoldiers()<2&& closest(visibleHills).food>parameters.ANT_COST*(1+numberofcreatedants)){
								createAnt(Caste.Soldier, Rapoport.class);
								numberofcreatedants++;
								haufen.setsoldiers(haufen.getsoldiers()+1);
							}
						}	
					}
					if(check){
						if(haufen.getmaxants()>haufen.getants()){
							if(phase<3){		
								phase++;
								haufen.setants(haufen.getants()+lostsouls);
								check=false;
								position=haufen.getPosition();
								while(haufen.getmaxants()>haufen.getants()&& closest(visibleHills).food>parameters.ANT_COST*(1+numberofcreatedants)){
									createAnt(Caste.Gatherer, Mueller.class);
									numbergatherer++;
									numberofcreatedants++;
									haufen.setants(haufen.getants()+1);
								}
						}else{
							phase=0;
							haufen.setsoldiers(haufen.getsoldiers()+lostsoldiers);
							check=false;
							position=haufen.getPosition();
							if(closest(visibleHills).food>parameters.ANT_COST){
								createAnt(Caste.Soldier, Rapoport.class);
								numberofcreatedants++;
								haufen.setsoldiers(haufen.getsoldiers()+1);
							}
						}
					}	
				}
				}
			}else{
				if(check && gegnerlist.size()>0){
					gegnerlist.get(0).setants(gegnerlist.get(0).getsoldiers()+lostsoldiers);
					position=gegnerlist.get(0).getPosition();
					check=false;
					sugarcheck=false;
					//	while(closest(visibleHills).food>parameters.ANT_COST*(1+numberofcreatedants)){
					if(closest(visibleHills).food>parameters.ANT_COST*1){
						createAnt(Caste.Soldier, Rapoport.class);
						numberofcreatedants++;
						gegnerlist.get(0).setsoldiers(gegnerlist.get(0).getsoldiers()+1);
					}
				}
			}
			for(Haufen haufen : sugarlist){	
				if(check){
					if(haufen.getsoldiers()<3){
						haufen.setants(haufen.getsoldiers()+lostsoldiers);
						check=false;
						position=haufen.getPosition();
						while(haufen.getsoldiers()<2&& closest(visibleHills).food>(1+numberofcreatedants)*parameters.ANT_COST){
							createAnt(Caste.Soldier, Rapoport.class);
							numberofcreatedants++;
							haufen.setsoldiers(haufen.getsoldiers()+1);
						}
					}
				}	
			}
			if(check==false && sugarcheck==true){
				talk(((int)((Math.round(position.x+parameters.SIZE_X/2))*Math.round(parameters.SIZE_Y)*10)+((int)((Math.round(position.y+parameters.SIZE_Y/2))*10+1))));
			}
			if(sugarcheck==false){
				talk(((int)((Math.round(position.x+parameters.SIZE_X/2))*Math.round(parameters.SIZE_Y)*10)+((int)((Math.round(position.y+parameters.SIZE_Y/2))*10+2))));
			}
		}
	}	

}
