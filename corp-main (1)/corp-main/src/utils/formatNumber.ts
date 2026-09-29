export function formatNumber(num) {
  const rounded = parseFloat(num.toFixed(2));
  return rounded % 1 === 0 ? Math.round(rounded) : rounded;
}
