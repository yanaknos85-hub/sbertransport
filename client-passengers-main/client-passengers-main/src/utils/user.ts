// Current user is expected internal if basic authentication is not used.
// Basic authentication flag (_IS_BASIC_AUTH) is set globally in host frontend app.
// eslint-disable-next-line no-underscore-dangle
export const isCurrentUserInternal = () => !window._IS_BASIC_AUTH;
