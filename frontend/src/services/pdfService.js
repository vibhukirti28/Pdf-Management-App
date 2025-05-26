import api from './api';

const PdfService = {
  uploadPdf: (formData) => {
    return api.post('/pdf/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  searchMyFiles: (query) => {
    return api.get(`/pdf/my-files/search?q=${encodeURIComponent(query)}`);
  },

  getMyFiles: () => {
    return api.get('/pdf/my-files');
  },

  searchFiles: (filename) => {
    return api.get(`/pdf/search?filename=${filename}`);
  },

  getPdfDetails: (id) => {
    return api.get(`/pdf/${id}`);
  },

  downloadPdf: (id) => {
    return api.get(`/pdf/download/${id}`, { responseType: 'blob' });
  },

  getComments: (pdfId) => {
    return api.get(`/pdf/${pdfId}/comments`);
  },

  addComment: (pdfId, commentData) => {
    return api.post(`/pdf/${pdfId}/comments`, commentData);
  },

  // Sharing related - if needed later
  shareFile: (pdfId) => {
    return api.post(`/shared/generate/${pdfId}`);
  },

  accessSharedFile: (shareToken) => {
    return api.get(`/shared/access/${shareToken}`);
  },

  downloadSharedFile: (shareToken) => {
    return api.get(`/shared/download/${shareToken}`, { responseType: 'blob' });
  },

  addSharedFileComment: (shareToken, commentData) => {
    // commentData should be an object like { username: "Guest Name", text: "This is a comment" }
    return api.post(`/shared/${shareToken}/comments`, commentData);
  }
};

export default PdfService;
