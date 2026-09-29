export const formatDriverRating = (rating: number | undefined | null): number | string => rating ? (rating / 100).toFixed(2) : '-';
