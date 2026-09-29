export const isTestStand = () => {
  const testStandPrefixes = ['.platform.', '.taxi.', '.cargo.', '.fleet.', 'st.', '.nt.', '.ift.', 'localhost'];

  return testStandPrefixes.some(prefix => window.location.origin.includes(prefix));
};
