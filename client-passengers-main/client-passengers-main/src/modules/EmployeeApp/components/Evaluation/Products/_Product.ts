import { action, observable } from 'mobx';
import { FC } from 'react';
import { ITripStore, TripRequest } from 'stores/Trip/Trip.interface';
import { IProduct } from '../types';
import Process from '../Constants/Process';

/**
 * 3 из 3
 *
 * Почему я сделал абстрактное взаимодействие и здесь? Большая часть логики общая и умещается именно здесь, в абстрактном классе.
 * Уникальная логика выносится в конкретные классы продуктов и перезаписывает абстрактные классы.
 *
 * Почему я выбрал observable поля и actions mobx? Это самое удобное решение для реактивных полей. Изменил одно поле - поменялось везде.
 * Фактически это state реакта но более доступный из всех мест и без side эффектов.
 * **/

abstract class Product implements IProduct {
  constructor(tripStore: ITripStore, request: TripRequest) {
    this.rateTripRequest = tripStore.rateTripRequest;

    this.setRating(request?.requestRating?.rating ?? 4);
  }

  rateTripRequest!: ITripStore['rateTripRequest'];

  @observable
    advantages?: string[];

  abstract setAdvantages(tab: number): void;
  @action.bound
  public clearAdvantages() {
    this.advantages = [];
  }

  @observable
    drawbacks?: string[];

  abstract setDrawbacks(tab: number): void;
  @action.bound
  public clearDrawbacks() {
    this.drawbacks = [];
  }

  @observable
    rating?: number;

  @action.bound
  public setRating(rating: number) {
    this.rating = rating;
  }

  @observable
    isPositive = true;

  @action.bound
  public setIsPositive(isPositive: boolean) {
    this.isPositive = isPositive;
  }

  @observable
    ratingComment?: string;

  @action.bound
  public setRatingComment(comment: string) {
    this.ratingComment = comment;
  }

  @observable
    process = Process.CLOSE;

  @action.bound
  private setProcess(process: Process) {
    this.process = process;
  }

  @observable
    icons = { advantages: {}, drawbacks: {} };

  @action.bound
  public setIcons(icons?: { advantages: Record<string, FC>; drawbacks: Record<string, FC> }) {
    if (icons) {
      this.icons = icons;
    }
  }

  @observable
    iconTitlesMap?: Dictionary<string> = {};

  @action.bound
  public setIconTitlesMap(map: Dictionary<string>) {
    this.iconTitlesMap = map;
  }

  @observable
    advantageTitles: Dictionary<string> = {};

  @action.bound
  public setAdvantageTitles(dic: Dictionary<string>) {
    this.advantageTitles = dic;
  }

  @observable
    drawbackTitles: Dictionary<string> = {};

  @action.bound
  public setDrawbackTitles(dic: Dictionary<string>) {
    this.drawbackTitles = dic;
  }

  @action.bound
  public onOpen() {
    this.setProcess(Process.START);
  }

  @action.bound
  public onClose() {
    this.setProcess(Process.CLOSE);
  }

  @action.bound
  public onSuccess(data: any, reqId: any) {
    return this.rateTripRequest(data, reqId)
      .then(() => {
        this.setProcess(Process.POSITIVE);
        return 200;
      });
  }

  @action.bound
  public onFailure(data: any, reqId: any) {
    return this.rateTripRequest(data, reqId)
      .then(() => {
        this.setProcess(Process.NEGATIVE);
        return 200;
      });
  }
}

export default Product;
