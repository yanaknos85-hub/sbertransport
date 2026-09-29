export const formatDistance = (distance: number) => {
  const roundedDistance = Math.round(distance);
  const formattedDistance = `${roundedDistance} км`;
  return formattedDistance;
};
