import React, { useState, useEffect, useCallback } from 'react';
import PdfService from '../services/pdfService';
import { Link } from 'react-router-dom';

const DashboardPage = () => {
  const [pdfs, setPdfs] = useState([]);
  const [isLoading, setIsLoading] = useState(true); // General loading for PDF list
  const [error, setError] = useState(null); // General error for PDF list

  // Search states
  const [searchQuery, setSearchQuery] = useState('');
  const [isSearching, setIsSearching] = useState(false);
  const [searchResults, setSearchResults] = useState([]);
  const [isSearchActive, setIsSearchActive] = useState(false);

  // Upload states
  const [selectedFile, setSelectedFile] = useState(null);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState('');
  const [uploadSuccessMessage, setUploadSuccessMessage] = useState('');

  // Refactored PDF fetching logic
  const fetchPdfs = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await PdfService.getMyFiles();
      setPdfs(response.data);
    } catch (err) {
      console.error("Failed to fetch PDFs:", err);
      setError(err.response?.data?.message || err.message || 'Failed to fetch PDFs.');
      setPdfs([]);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchPdfs();
  }, [fetchPdfs]);

  const handleFileChange = (event) => {
    setSelectedFile(event.target.files[0]);
    setUploadError('');
    setUploadSuccessMessage('');
  };

  const handleUpload = async (event) => {
    event.preventDefault();
    if (!selectedFile) {
      setUploadError('Please select a PDF file to upload.');
      return;
    }

    setIsUploading(true);
    setUploadError('');
    setUploadSuccessMessage('');

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
      await PdfService.uploadPdf(formData);
      setUploadSuccessMessage(`Successfully uploaded "${selectedFile.name}"!`);
      fetchPdfs(); // Refresh the list of PDFs
    } catch (err) {
      console.error("Failed to upload PDF:", err);
      setUploadError(err.response?.data?.message || err.message || 'Failed to upload PDF.');
    } finally {
      setIsUploading(false);
      setSelectedFile(null); // Clear the selected file state
      // Clear the file input visually
      if (document.getElementById('pdf-upload-input')) {
        document.getElementById('pdf-upload-input').value = null;
      }
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchQuery.trim()) {
      setIsSearchActive(false);
      setSearchResults([]); // Clear previous search results
      setError(null); // Clear any errors
      return;
    }
    setIsLoading(true); 
    setIsSearching(true); 
    setIsSearchActive(true);
    setError(null); 
    setUploadError(''); 
    setUploadSuccessMessage('');

    try {
      const response = await PdfService.searchMyFiles(searchQuery);
      setSearchResults(response.data);
    } catch (err) {
      console.error("Failed to search PDFs:", err);
      setError(err.response?.data?.message || err.message || 'Failed to search PDFs.');
      setSearchResults([]);
    } finally {
      setIsLoading(false);
      setIsSearching(false);
    }
  };

  const clearSearch = () => {
    setSearchQuery('');
    setIsSearchActive(false);
    setSearchResults([]);
    setError(null);
  };

  return (
    <div className="container dashboard-page">
      <h2>My PDF Dashboard</h2>

      <div className="dashboard-actions" style={{ marginBottom: '20px' }}>
        {/* PDF Upload Section */}
        <div className="upload-section" style={{ marginBottom: '20px', padding: '15px', border: '1px solid #eee', borderRadius: '5px' }}>
          <h4>Upload New PDF</h4>
          <form onSubmit={handleUpload}>
            <input 
              type="file" 
              id="pdf-upload-input"
              accept=".pdf" 
              onChange={handleFileChange} 
              disabled={isUploading}
              style={{ marginRight: '10px' }}
            />
            <button type="submit" disabled={isUploading || !selectedFile}>
              {isUploading ? 'Uploading...' : 'Upload PDF'}
            </button>
          </form>
          {isUploading && <p style={{ marginTop: '5px' }}>Uploading, please wait...</p>}
          {uploadError && <p className="error-message" style={{ color: 'red', marginTop: '5px' }}>{uploadError}</p>}
          {uploadSuccessMessage && <p className="success-message" style={{ color: 'green', marginTop: '5px' }}>{uploadSuccessMessage}</p>}
        </div>

        {/* Search Section */}
        <form onSubmit={handleSearch} className="search-form">
          <input 
            type="text" 
            placeholder="Search your PDFs by name..." 
            value={searchQuery} 
            onChange={(e) => setSearchQuery(e.target.value)} 
            className="search-input"
            disabled={isLoading || isUploading}
          />
          <button type="submit" className="search-button" disabled={isLoading || isSearching || isUploading}>
            {isSearching ? 'Searching...' : 'Search'}
          </button>
          {isSearchActive && (
            <button type="button" onClick={clearSearch} className="clear-search-button" disabled={isLoading || isSearching || isUploading}>
              Clear
            </button>
          )}
        </form>
      </div>

      {/* PDF List Section */}
      {(isLoading && !isSearching && !isUploading) && <p>Loading your PDFs...</p>} 
      {error && <p className="error-message">Error: {error}</p>}
      
      {!isLoading && !error && (
        <div className="pdf-list-container">
          {isSearchActive ? (
            searchResults.length === 0 && !isSearching ? (
              <p>No PDFs found for "{searchQuery}".</p>
            ) : (
              <ul className="pdf-list search-results">
                {searchResults.map(pdf => (
                  <li key={pdf.id} className="pdf-list-item">
                    <Link to={`/pdf/${pdf.id}`} className="pdf-filename">{pdf.filename}</Link>
                    <span className="pdf-upload-time">Uploaded: {new Date(pdf.uploadTime).toLocaleDateString()}</span>
                  </li>
                ))}
              </ul>
            )
          ) : (
            pdfs.length === 0 && !isUploading ? (
              <p>You haven't uploaded any PDFs yet. Use the form above to upload your first PDF!</p>
            ) : (
              <ul className="pdf-list">
                {pdfs.map(pdf => (
                  <li key={pdf.id} className="pdf-list-item">
                    <Link to={`/pdf/${pdf.id}`} className="pdf-filename">{pdf.filename}</Link>
                    <span className="pdf-upload-time">Uploaded: {new Date(pdf.uploadTime).toLocaleDateString()}</span>
                  </li>
                ))}
              </ul>
            )
          )}
        </div>
      )}
    </div>
  );
};

export default DashboardPage;
