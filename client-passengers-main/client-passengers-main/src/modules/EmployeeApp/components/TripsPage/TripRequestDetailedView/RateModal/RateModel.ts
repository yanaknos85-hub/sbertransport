import { IRequestRating } from 'stores/Trip/Trip.interface';

export class RateModel implements IRequestRating {
  rating?: number;

  advantages?: string[];

  drawbacks?: string[];

  ratingComment?: string;

  constructor(request: IRequestRating) {
    this.rating = request.rating;
    this.advantages = request.advantages;
    this.drawbacks = request.drawbacks;
    this.ratingComment = request.ratingComment;
  }
}
