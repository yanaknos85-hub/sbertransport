export enum RATES {
  NORATE,
  AWFUL,
  BAD,
  NORMAL,
  GOOD,
  GREAT,
}

export enum RATES_NAMES {
  NORATE = 'Нет оценки',
  AWFUL = 'Ужасно',
  BAD = 'Плохо',
  NORMAL = 'Нормально',
  GOOD = 'Хорошо',
  GREAT = 'Великолепно',
}

export type RATES_TYPE = keyof typeof RATES_NAMES;

export enum NegativeEmotions {
  aggressive = 'aggressive',
  dirty = 'dirty',
  rude = 'rude',
  late = 'late',
}

export enum NegativeEmotionsPersonal {
  unclear = 'unclear',
  notConvenient = 'notConvenient',
  slow = 'slow',
}

export enum NegativeEmotionsCarsharing {
  faultyCar = 'faultyCar',
  poorSupport = 'poorSupport',
  dirtyCar = 'dirtyCar',
  noNecessaryDocuments = 'noNecessaryDocuments',
}

export enum NegativeEmotionsPublic {
  unclear = 'unclear',
  notConvenient = 'notConvenient',
  slow = 'slow',
}

export enum PositiveEmotions {
  careful = 'careful',
  clean = 'clean',
  polite = 'polite',
  goodMood = 'goodMood',
}

export enum PositiveEmotionsPersonal {
  clearly = 'clearly',
  comfortable = 'comfortable',
  quickly = 'quickly',
}

export enum PositiveEmotionsCarsharing {
  serviceableCar = 'serviceableCar',
  convenientSupport = 'convenientSupport',
  cleanCar = 'cleanCar',
  allDocuments = 'allDocuments',
}

export enum PositiveEmotionsPublic {
  clearly = 'clearly',
  comfortable = 'comfortable',
  quickly = 'quickly',
}

export type TNegativeEmotionsKeys = keyof typeof NegativeEmotions;
export type TPositiveEmotionsKeys = keyof typeof PositiveEmotions;

export const NegativeEmotionsTitles = {
  [NegativeEmotions.aggressive]: 'Агрессивное вождение',
  [NegativeEmotions.dirty]: 'Грязный салон',
  [NegativeEmotions.late]: 'Опоздал',
  [NegativeEmotions.rude]: 'Грубость',
};

export const NegativeEmotionsTitlesPersonal = {
  [NegativeEmotionsPersonal.unclear]: 'Непонятно',
  [NegativeEmotionsPersonal.notConvenient]: 'Не удобно',
  [NegativeEmotionsPersonal.slow]: 'Медленно',
};

export const NegativeEmotionsTitlesCarsharing = {
  [NegativeEmotionsCarsharing.faultyCar]: 'Неисправный автомобиль',
  [NegativeEmotionsCarsharing.poorSupport]: 'Плохая поддержка',
  [NegativeEmotionsCarsharing.dirtyCar]: 'Грязная машина',
  [NegativeEmotionsCarsharing.noNecessaryDocuments]: 'Нет нужных документов',
};

export const NegativeEmotionsTitlesPublic = {
  [NegativeEmotionsPublic.unclear]: 'Непонятно',
  [NegativeEmotionsPublic.notConvenient]: 'Не удобно',
  [NegativeEmotionsPublic.slow]: 'Медленно',
};

export const PositiveEmotionsTitles = {
  [PositiveEmotions.careful]: 'Аккуратное вождение',
  [PositiveEmotions.clean]: 'Чистый салон',
  [PositiveEmotions.polite]: 'Вежливость',
  [PositiveEmotions.goodMood]: 'Хорошее настроение',
};

export const PositiveEmotionsTitlesPersonal = {
  [PositiveEmotionsPersonal.clearly]: 'Понятно',
  [PositiveEmotionsPersonal.comfortable]: 'Удобно',
  [PositiveEmotionsPersonal.quickly]: 'Быстро',
};

export const PositiveEmotionsTitlesCarsharing = {
  [PositiveEmotionsCarsharing.serviceableCar]: 'Исправный автомобиль',
  [PositiveEmotionsCarsharing.convenientSupport]: 'Удобная поддержка',
  [PositiveEmotionsCarsharing.cleanCar]: 'Чистая машина',
  [PositiveEmotionsCarsharing.allDocuments]: 'Есть все документы',
};

export const PositiveEmotionsTitlesPublic = {
  [PositiveEmotionsPublic.clearly]: 'Понятно',
  [PositiveEmotionsPublic.comfortable]: 'Удобно',
  [PositiveEmotionsPublic.quickly]: 'Быстро',
};

export const RateFeedBackTitles = {
  positiveFeedBack: 'Что Вам особенно понравилось?',
  negativeFeedBack: 'Что было не так?',
  comment: 'Что бы Вы отметили?',
};
