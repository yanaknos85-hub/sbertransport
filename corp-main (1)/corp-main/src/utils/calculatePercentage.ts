export function calculatePercentage(mainNumber, otherNumbers) {
  const count = mainNumber + otherNumbers.reduce((accumulator, currentValue) => accumulator + currentValue, 0);
  return (mainNumber / count) * 100;
}
