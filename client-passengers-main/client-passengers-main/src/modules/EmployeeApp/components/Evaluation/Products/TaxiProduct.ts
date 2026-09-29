import { action } from 'mobx';
import { ITripStore, TripRequest } from 'stores/Trip/Trip.interface';
import {
  ADVANTAGES,
  ADVANTAGES_NUMBERS,
  ADVANTAGES_TITLES,
  DRAWBACKS,
  DRAWBACKS_NUMBERS,
  DRAWBACKS_TITLES
} from '../Constants/TaxiMoods';
import { IIcons } from '../types';
import { icons } from '../View/icons';
import iconTitlesMap from '../View/iconsTitlesMap';
import Product from './_Product';

class TaxiProduct extends Product {
  constructor(tripStore: ITripStore, request: TripRequest) {
    super(tripStore, request);

    this.setIcons(icons['TAXI'] as IIcons);
    this.setIconTitlesMap(iconTitlesMap['TAXI']);
    this.setAdvantageTitles(ADVANTAGES_TITLES);
    this.setDrawbackTitles(DRAWBACKS_TITLES);

    request?.requestRating?.advantages?.forEach(advantage => {
      this.setAdvantages(ADVANTAGES_NUMBERS[advantage as unknown as ADVANTAGES_NUMBERS] as unknown as number);
    });

    request?.requestRating?.drawbacks?.forEach(drawback => {
      this.setDrawbacks(DRAWBACKS_NUMBERS[drawback as unknown as DRAWBACKS_NUMBERS] as unknown as number);
    });
  }

  @action.bound
  setAdvantages(tab: number): void {
    let advantage: ADVANTAGES;

    switch (tab) {
      case 0: {
        advantage = ADVANTAGES.SAFETY_DRIVING;
        break;
      }
      case 1: {
        advantage = ADVANTAGES.CLEARNESS;
        break;
      }
      case 2: {
        advantage = ADVANTAGES.POLITENESS;
        break;
      }
      case 3: {
        advantage = ADVANTAGES.GOOD_MOOD;
        break;
      }
      default: {
        advantage = ADVANTAGES.SAFETY_DRIVING;
      }
    }

    const isFound = this.advantages?.includes(advantage);

    if (isFound) {
      this.advantages = this.advantages?.filter(item => item !== advantage);
    } else {
      this.advantages = this.advantages ? [...this.advantages, advantage] : [advantage];
    }
  }

  @action.bound
  setDrawbacks(tab: number): void {
    let drawBack: DRAWBACKS;

    switch (tab) {
      case 0: {
        drawBack = DRAWBACKS.AGGRESSIVE_DRIVING;
        break;
      }
      case 1: {
        drawBack = DRAWBACKS.DIRTY_SALON;
        break;
      }
      case 2: {
        drawBack = DRAWBACKS.LATE;
        break;
      }
      case 3: {
        drawBack = DRAWBACKS.RUDE;
        break;
      }
      default: {
        drawBack = DRAWBACKS.AGGRESSIVE_DRIVING;
      }
    }

    const isFound = this.drawbacks?.includes(drawBack);

    if (isFound) {
      this.drawbacks = this.drawbacks?.filter(item => item !== drawBack);
    } else {
      this.drawbacks = this.drawbacks ? [...this.drawbacks, drawBack] : [drawBack];
    }
  }
}

export default TaxiProduct;
