export const formatDriverRating = (rating: number | undefined | null, emptyValue = '-'): number | string => (
  rating ? (rating / 100).toFixed(2) : emptyValue
);
