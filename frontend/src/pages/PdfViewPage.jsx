import React, { useState } from 'react';
import { useParams } from 'react-router-dom';

const dummyComments = [
  { id: 1, user: 'Alice', text: 'Great PDF, thanks for sharing!', timestamp: '2024-05-25 10:00' },
  { id: 2, user: 'Bob', text: 'Found a typo on page 3.', timestamp: '2024-05-25 11:30' },
];

const PdfViewPage = () => {
  const { id } = useParams();
  const [newComment, setNewComment] = useState('');
  const [comments, setComments] = useState(dummyComments); // Replace with API call

  const handleCommentSubmit = (e) => {
    e.preventDefault();
    if (newComment.trim() === '') return;
    // Logic to submit comment to backend should be integrated here
    console.log(`Submitting comment for PDF ${id}: ${newComment}`);
    const newCommentObj = {
      id: comments.length + 1, // Temporary id, replace with backend-generated id
      user: 'CurrentUser', // Replace with actual user from auth context
      text: newComment,
      timestamp: new Date().toLocaleString(),
    };
    setComments([...comments, newCommentObj]);
    setNewComment('');
  };

  return (
    <div className="pdf-view-container" style={styles.container}>
      <div className="pdf-viewer-area" style={styles.pdfArea}>
        <h2>PDF Viewer - File ID: {id}</h2>
        {/* PDF iframe/viewer component will go here. 
            The src should point to your PDF serving API endpoint or a URL to the PDF file. 
            Example: src={`/api/pdf/file/${id}`} or src={pdfUrlFromState} */}
        <iframe 
          src={`https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf`} // Placeholder PDF URL
          style={styles.pdfIframe}
          title={`PDF ${id}`}
        ></iframe>
      </div>

      <div className="comments-section" style={styles.commentsSection}>
        <h3>Comments</h3>
        <div className="comments-list" style={styles.commentsList}>
          {comments.length === 0 ? (
            <p>No comments yet. Be the first to comment!</p>
          ) : (
            comments.map(comment => (
              <div key={comment.id} className="comment-item" style={styles.commentItem}>
                <div style={styles.commentHeader}>
                  <strong style={styles.commentUser}>{comment.user}</strong>
                  <span style={styles.commentTimestamp}>{comment.timestamp}</span>
                </div>
                <p style={styles.commentText}>{comment.text}</p>
              </div>
            ))
          )}
        </div>
        <form onSubmit={handleCommentSubmit} className="comment-form" style={styles.commentForm}>
          <textarea
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            placeholder="Write a comment..."
            rows="3"
            style={styles.commentTextarea}
          />
          <button type="submit" style={styles.commentSubmitButton}>Post Comment</button>
        </form>
      </div>
    </div>
  );
};

// Basic styling - consider moving to a separate CSS file for a real application
const styles = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    gap: '24px',
    padding: '24px',
    fontFamily: '"Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
    maxWidth: '960px',
    margin: '20px auto',
    boxShadow: '0 6px 12px rgba(0,0,0,0.08)',
    borderRadius: '12px',
    backgroundColor: '#fcfcfc',
  },
  pdfArea: {
    border: '1px solid #e9ecef',
    padding: '24px',
    borderRadius: '8px',
    backgroundColor: '#ffffff',
  },
  pdfIframe: {
    width: '100%',
    height: '650px',
    border: '1px solid #dee2e6',
    borderRadius: '4px',
  },
  commentsSection: {
    border: '1px solid #e9ecef',
    padding: '24px',
    borderRadius: '8px',
    backgroundColor: '#ffffff',
  },
  commentsList: {
    maxHeight: '400px',
    overflowY: 'auto',
    marginBottom: '20px',
    paddingRight: '10px', 
  },
  commentItem: {
    borderBottom: '1px solid #f1f3f5',
    padding: '16px 0',
    marginBottom: '12px',
  },
  commentHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '8px',
  },
  commentUser: {
    fontSize: '1em',
    color: '#212529',
    fontWeight: '600',
  },
  commentTimestamp: {
    fontSize: '0.8em',
    color: '#6c757d',
  },
  commentText: {
    margin: 0,
    fontSize: '0.95em',
    color: '#495057',
    lineHeight: '1.65',
    whiteSpace: 'pre-wrap', // Preserve line breaks in comments
  },
  commentForm: {
    display: 'flex',
    flexDirection: 'column',
    gap: '12px',
  },
  commentTextarea: {
    width: '100%',
    padding: '12px 16px',
    borderRadius: '6px',
    border: '1px solid #ced4da',
    boxSizing: 'border-box',
    resize: 'vertical',
    minHeight: '80px',
    fontFamily: 'inherit',
    fontSize: '0.95em',
    lineHeight: '1.5',
  },
  commentSubmitButton: {
    padding: '10px 20px',
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    alignSelf: 'flex-end',
    fontWeight: '500',
    fontSize: '0.95em',
    transition: 'background-color 0.2s ease-in-out, box-shadow 0.2s ease',
  },
  // For hover effect on button, you'd typically use a CSS class:
  // '.comment-submit-button:hover': {
  //   backgroundColor: '#0056b3',
  //   boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
  // }
};

export default PdfViewPage;
