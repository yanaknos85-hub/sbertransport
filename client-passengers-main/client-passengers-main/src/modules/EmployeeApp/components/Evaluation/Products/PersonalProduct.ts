import { action } from 'mobx';
import { ITripStore, TripRequest } from 'stores/Trip/Trip.interface';
import {
  ADVANTAGES,
  ADVANTAGES_NUMBERS,
  ADVANTAGES_TITLES,
  DRAWBACKS,
  DRAWBACKS_NUMBERS,
  DRAWBACKS_TITLES
} from '../Constants/PersonalMoods';
import { IIcons } from '../types';
import { icons } from '../View/icons';
import iconTitlesMap from '../View/iconsTitlesMap';
import Product from './_Product';

class PersonalProduct extends Product {
  constructor(tripStore: ITripStore, request: TripRequest) {
    super(tripStore, request);

    this.setIcons(icons['PERSONAL'] as IIcons);
    this.setIconTitlesMap(iconTitlesMap['PERSONAL']);
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
        advantage = ADVANTAGES.COMPREHENSIBLY;
        break;
      }
      case 1: {
        advantage = ADVANTAGES.CONVENIENTLY;
        break;
      }
      case 2: {
        advantage = ADVANTAGES.QUICKLY;
        break;
      }
      default: {
        advantage = ADVANTAGES.COMPREHENSIBLY;
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
        drawBack = DRAWBACKS.INCOMPREHENSIBLY;
        break;
      }
      case 1: {
        drawBack = DRAWBACKS.INCONVENIENTLY;
        break;
      }
      case 2: {
        drawBack = DRAWBACKS.SLOWLY;
        break;
      }
      default: {
        drawBack = DRAWBACKS.INCOMPREHENSIBLY;
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

export default PersonalProduct;
