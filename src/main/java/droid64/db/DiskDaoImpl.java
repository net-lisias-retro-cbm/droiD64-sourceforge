package droid64.db;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MySQL implementation of DiskDao. 
 * @author Henrik
 */
public class DiskDaoImpl implements DiskDao {

	public DiskDaoImpl() {	
	}

	@Override
	public List<Disk> getAllDisks() throws DatabaseException {
		String sql = "SELECT " + getColumnNames() + " FROM disk";
		PreparedStatement stmt = DaoFactoryImpl.prepareStatement(sql);
		try {
			ResultSet rs = stmt.executeQuery();
			return consumeRows(rs);
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}		
	}

	@Override
	public Disk getDisk(long diskId) throws DatabaseException {
		String sql = "SELECT " + getColumnNames() + " FROM disk WHERE diskid=?";
		PreparedStatement stmt = DaoFactoryImpl.prepareStatement(sql);
		try {
			stmt.setLong(1, diskId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return consumeRow(rs);
			} else {
				throw new NotFoundException("No such diskId ("+diskId+").");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public Disk getDiskByFileName(String fileName) throws DatabaseException {
		String sql = "SELECT " + getColumnNames() + " FROM disk WHERE filepath=? AND filename=?";
		File f = new File(fileName);
		File p = f.getAbsoluteFile().getParentFile();
		String path = p != null ? p.getAbsolutePath() : null;
		String file = f.getName();
		PreparedStatement stmt = DaoFactoryImpl.prepareStatement(sql);
		try {
			stmt.setString(1, path);
			stmt.setString(2, file);
			
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return consumeRow(rs);
			} else {
				throw new NotFoundException("No such disk ("+fileName+").");
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	@Override
	public void update(Disk disk) throws DatabaseException {
		if (disk == null) {
			throw new DatabaseException("Null argument.");
		}
		String sql = "UPDATE disk SET(" + getUpdateColumnNames() + ") WHERE diskid=?";
		PreparedStatement stmt = DaoFactoryImpl.prepareStatement(sql);
		try {
			stmt.setString(1, disk.getLabel());
			stmt.setString(2, disk.getFilePath());
			stmt.setString(3, disk.getFileName());
			stmt.setLong(4, disk.getDiskId());
			stmt.setInt(5, disk.getImageType());
			setInteger(stmt, 6, disk.getErrors());
			setInteger(stmt, 7, disk.getWarnings());
			if (1 != stmt.executeUpdate()) {
				throw new NotFoundException("DiskId "+disk.getDiskId()+" could not be updated.");
			}
		}  catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public void delete(Disk disk) throws DatabaseException {
		if (disk == null) {
			throw new DatabaseException("Null argument.");
		}
		String sql = "DELETE FROM diskfile WHERE diskid=?";
		PreparedStatement stmt = DaoFactoryImpl.prepareStatement(sql);
		try {
			stmt.setLong(1, disk.getDiskId());
			stmt.executeUpdate();
		}  catch (SQLException e) {
			throw new DatabaseException(e);
		}
		sql = "DELETE FROM disk WHERE diskId=?";
		stmt = DaoFactoryImpl.prepareStatement(sql);
		try {
			stmt.setLong(1, disk.getDiskId());
			if (1 != stmt.executeUpdate()) {
				throw new NotFoundException("Nothing was deleted");
			}
		}  catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public List<Disk> search(DiskSearchCriteria criteria) throws DatabaseException {
		List<Disk> result = new ArrayList<Disk>();
		StringBuilder sqlBuf = new StringBuilder();
		sqlBuf.append("SELECT d.diskid, d.filepath, d.filename, d.label, df.fileid, df.name, df.filetype, df.size, df.fileNum, df.flags, d.updated, d.imagetype, d.errors, d.warnings FROM diskfile df ");
		sqlBuf.append("JOIN disk d ON df.diskid = d.diskid ");
		if (criteria.hasCriteria()) {
			String and = "";
			sqlBuf.append("WHERE ");
			if (!isStringNullOrEmpty(criteria.getFileName())) {
				sqlBuf.append("UPPER(df.name) LIKE ? ");
				and = "AND ";
			}
			if (!isStringNullOrEmpty(criteria.getDiskLabel())) {
				sqlBuf.append(and).append("UPPER(d.label) LIKE ? ");
				and = "AND ";
			}
			if (!isStringNullOrEmpty(criteria.getDiskPath())) {
				sqlBuf.append(and).append("UPPER(d.filepath) LIKE ? ");
				and = "AND ";				
			}
			if (!isStringNullOrEmpty(criteria.getDiskFileName())) {
				sqlBuf.append(and).append("UPPER(d.filename) LIKE ? ");
				and = "AND ";				
			}
			if (criteria.getFileSizeMin()!=null) {
				sqlBuf.append(and).append("df.size >= ? ");
				and = "AND ";				
			}
			if (criteria.getFileSizeMax()!=null) {
				sqlBuf.append(and).append("df.size <= ? ");
				and = "AND ";				
			}			
			if (criteria.getFileType()!=null) {
				sqlBuf.append(and).append("df.filetype = ? ");
				and = "AND ";				
			}	
			if (criteria.getImageType()!=null) {
				sqlBuf.append(and).append("d.imagetype = ? ");
				and = "AND ";				
			}	
		}
		sqlBuf.append("ORDER BY d.filepath, d.filename, df.fileNum ");
		sqlBuf.append("LIMIT ?");

		try {
			String sql = sqlBuf.toString();
			PreparedStatement stmt = DaoFactoryImpl.prepareStatement(sql);
			int idx = 1;
			if (criteria.hasCriteria()) {
				if (!isStringNullOrEmpty(criteria.getFileName())) {
					stmt.setString(idx++, "%"+criteria.getFileName().toUpperCase()+"%");
				}
				if (!isStringNullOrEmpty(criteria.getDiskLabel())) {
					stmt.setString(idx++, "%"+criteria.getDiskLabel().toUpperCase()+"%");
				}
				if (!isStringNullOrEmpty(criteria.getDiskPath())) {
					stmt.setString(idx++, "%"+criteria.getDiskPath().toUpperCase()+"%");
				}
				if (!isStringNullOrEmpty(criteria.getDiskFileName())) {
					stmt.setString(idx++, "%"+criteria.getDiskFileName().toUpperCase()+"%");
				}
				if (criteria.getFileSizeMin()!=null) {
					stmt.setInt(idx++, criteria.getFileSizeMin());
				}
				if (criteria.getFileSizeMax()!=null) {
					stmt.setInt(idx++, criteria.getFileSizeMax());
				}
				if (criteria.getFileType()!=null) {
					stmt.setInt(idx++, criteria.getFileType());
				}
				if (criteria.getImageType()!=null) {
					stmt.setInt(idx++, criteria.getImageType());
				}
			}
			
			stmt.setLong(idx++, DaoFactoryImpl.getMaxRows());			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Disk disk = new Disk();
				DiskFile file = new DiskFile();
				disk.getFileList().add(file);
				disk.setDiskId(rs.getLong(1));
				disk.setFilePath(rs.getString(2));
				disk.setFileName(rs.getString(3));
				disk.setLabel(rs.getString(4));
				file.setFileId(rs.getLong(5));
				file.setName(rs.getString(6));
				file.setFileType(rs.getInt(7));
				file.setSize(rs.getInt(8));
				file.setFileNum(rs.getInt(9));
				file.setFlags(rs.getInt(10));
				Timestamp updated = rs.getTimestamp(11);
				disk.setUpdated(updated!=null ? new Date(updated.getTime()) : null);
				disk.setImageType(rs.getInt(12));
				disk.setErrors(getInteger(rs, 13));
				disk.setWarnings(getInteger(rs, 14));
				result.add(disk);
			}
			return result;
		}  catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	@Override
	public void save(Disk disk) throws DatabaseException {
		if (disk == null) { return; }
		try {
			StringBuilder sqlBuf = new StringBuilder();
			sqlBuf.append("SELECT d.diskid, d.filepath, d.filename, d.label, df.fileid, df.name, df.filetype, df.size, df.fileNum, df.flags, d.imagetype, d.errors, d.warnings FROM disk d ");
			sqlBuf.append("LEFT JOIN diskfile df ON df.diskid = d.diskid ");
			sqlBuf.append("WHERE d.filePath = ? AND d.filename = ? ");
			sqlBuf.append("ORDER BY d.filepath, d.filename, df.fileNum;\n ");
			PreparedStatement stmt = DaoFactoryImpl.prepareStatement(sqlBuf.toString());
			int idx = 1;
			stmt.setString(idx++, disk.getFilePath());
			stmt.setString(idx++, disk.getFileName());
			ResultSet rs = stmt.executeQuery();
			Disk oldDisk = null;		
			while (rs.next()) {
				if (oldDisk == null) {
					oldDisk = new Disk();
					oldDisk.setClean();
					oldDisk.setDiskId(rs.getLong(1));
					oldDisk.setFilePath(rs.getString(2));
					oldDisk.setFileName(rs.getString(3));
					oldDisk.setLabel(rs.getString(4));
					oldDisk.setImageType(rs.getInt(11));
					oldDisk.setErrors(getInteger(rs, 12));
					oldDisk.setWarnings(getInteger(rs, 13));
				}
				long fileId = rs.getLong(5);
				if (fileId != 0L) {
					DiskFile oldFile = new DiskFile();
					oldFile.setClean();
					oldFile.setFileId(fileId);
					oldFile.setDiskId(rs.getLong(1));
					oldFile.setName(rs.getString(6));
					oldFile.setFileType(rs.getInt(7));
					oldFile.setSize(rs.getInt(8));
					oldFile.setFileNum(rs.getInt(9));
					oldFile.setFlags(rs.getInt(10));
					oldDisk.getFileList().add(oldFile);
				}
			}		
			if (oldDisk != null) {
				disk.setDiskId(oldDisk.getDiskId());
				disk.setUpdate();				
				int longestList = disk.getFileList().size() > oldDisk.getFileList().size() ? disk.getFileList().size() : oldDisk.getFileList().size();
				int newFileCount = disk.getFileList().size();
				int oldfileCount = oldDisk.getFileList().size();
				for (int i = 0; i < longestList; i++) {
					if (i < newFileCount && i < oldfileCount) {
						DiskFile newFile = disk.getFileList().get(i);
						DiskFile oldFile = oldDisk.getFileList().get(i);
						newFile.setDiskId(disk.getDiskId());
						newFile.setFileId(oldFile.getFileId());
						newFile.setUpdate();
					} else if (i >= newFileCount) {
						DiskFile oldFile = oldDisk.getFileList().get(i);
						oldFile.setDelete();
						disk.getFileList().add(oldFile);
					} else {
						disk.getFileList().get(i).setInsert();
						disk.getFileList().get(i).setDiskId(disk.getDiskId());
					}
				}
			} else {
				disk.setInsert();
				sqlBuf = new StringBuilder();
				for (DiskFile newFile : disk.getFileList()) {
					newFile.setInsert();
				}
			}
			performSave(disk);
		}  catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}
	
	/**
	 * Save disk
	 * @param disk Disk
	 * @throws DatabaseException
	 */
	private void performSave(Disk disk) throws DatabaseException {
		Connection conn = DaoFactoryImpl.getConnection();
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
		if (disk.isInsert()) {
			try {
				String sqlDisk = "INSERT INTO disk(label, filepath, filename, updated, imagetype, errors, warnings) VALUES(?,?,?,?,?,?,?)";
				PreparedStatement stmt = conn.prepareStatement(sqlDisk, Statement.RETURN_GENERATED_KEYS);				
				stmt.setString(1, disk.getLabel());
				stmt.setString(2, disk.getFilePath());
				stmt.setString(3, disk.getFileName());
				stmt.setTimestamp(4, new Timestamp(new Date().getTime()));
				stmt.setInt(5, disk.getImageType());
				setInteger(stmt, 6, disk.getErrors());
				setInteger(stmt, 7, disk.getWarnings());
				int rows = stmt.executeUpdate();
				if (rows == 0) {
					conn.rollback();
					throw new DatabaseException("Failed to insert new disk.");
				}
		        ResultSet generatedKeys = stmt.getGeneratedKeys();
		        if (generatedKeys.next()) {
		        	disk.setDiskId(generatedKeys.getLong(1));		            	
		        } else {
		        	conn.rollback();
		        	throw new DatabaseException("Failed to insert disk, no diskId obtained.");
		        }
		        disk.setClean();
				for (DiskFile file : disk.getFileList()) {
					file.setDiskId(disk.getDiskId());
					file.setInsert();
				}
			} catch (SQLException e1) {
				try {
					conn.rollback();
				} catch (SQLException e2) {}
				throw new DatabaseException(e1);
			}
		} else if (disk.isUpdate()) {
			try {
				String sqlDisk = "UPDATE disk SET label=?,filepath=?,filename=?,updated=?,imageType=?,errors=?,warnings=? WHERE diskid=?";
				PreparedStatement stmt = conn.prepareStatement(sqlDisk);				
				stmt.setString(1, disk.getLabel());
				stmt.setString(2, disk.getFilePath());
				stmt.setString(3, disk.getFileName());
				stmt.setTimestamp(4, new Timestamp(new Date().getTime()));
				stmt.setInt(5, disk.getImageType());
				setInteger(stmt, 6, disk.getErrors());
				setInteger(stmt, 7, disk.getWarnings());
				stmt.setLong(8, disk.getDiskId());
				int rows = stmt.executeUpdate();
				if (rows == 0) {
					conn.rollback();
					throw new DatabaseException("Failed to update disk.");
				}
		        disk.setClean();				
			} catch (SQLException e1) {
				try {
					conn.rollback();
				} catch (SQLException e2) {}
				throw new DatabaseException(e1);
			}
		} else if (disk.isDelete()) {
			try {
				PreparedStatement stmt;
				String sqlFile = "DELETE FROM diskfile WHERE diskid=?";
				stmt = conn.prepareStatement(sqlFile);			
				stmt.setLong(1, disk.getDiskId());
				stmt.executeUpdate();
				String sqlDisk = "DELETE FROM disk WHERE diskid=?";
				stmt = conn.prepareStatement(sqlDisk);				
				stmt.setLong(1, disk.getDiskId());
				stmt.executeUpdate();
			} catch (SQLException e1) {
				try {
					conn.rollback();
				} catch (SQLException e2) {}
				throw new DatabaseException(e1);
			}
		} else {
			boolean clean = true;
			for (DiskFile file : disk.getFileList()) {
				if (!file.isClean()) {
					clean = false;
					break;
				}
			}
			if (clean) {
				return;
			}
		}
		if (!disk.isDelete()) {
			try {
				for (DiskFile file : disk.getFileList()) {
					int idx = 1;
					if (file.isInsert()) {
						String sql = "INSERT INTO diskfile(diskid,name,filetype,size,filenum,flags) VALUES (?,?,?,?,?,?)";
						PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
						stmt.setLong(idx++, file.getDiskId());
						stmt.setString(idx++, file.getName());
						stmt.setInt(idx++, file.getFileType());
						stmt.setInt(idx++, file.getSize());
						stmt.setInt(idx++, file.getFileNum());
						stmt.setInt(idx++,  file.getFlags());
						stmt.executeUpdate();
					} else if (file.isUpdate()) {
						String sql = "UPDATE diskfile SET name=?,filetype=?,size=?,filenum=?,flags=? WHERE diskid=? AND fileid=?";
						PreparedStatement stmt = conn.prepareStatement(sql);
						stmt.setString(idx++, file.getName());
						stmt.setInt(idx++, file.getFileType());
						stmt.setInt(idx++, file.getSize());
						stmt.setInt(idx++, file.getFileNum());
						stmt.setInt(idx++, file.getFlags());
						stmt.setLong(idx++, file.getDiskId());
						stmt.setLong(idx++, file.getFileId());
						stmt.executeUpdate();
					} else if (file.isDelete()) {
						String sql= "DELETE FROM diskfile WHERE diskid=? AND fileid=?";
						PreparedStatement stmt = conn.prepareStatement(sql);
						stmt.setLong(idx++, file.getDiskId());
						stmt.setLong(idx++, file.getFileId());
						stmt.executeUpdate();
					}
					file.setClean();
				}				
			} catch (SQLException e) {
				try {
					conn.rollback();
				} catch (SQLException e1) {	}
				throw new DatabaseException(e);
			}
		}
		try {
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {}
			throw new DatabaseException(e);
		}
	}
	
	private String getColumnNames() {
		return "diskId, label, filePath, fileName, updated, imagetype, errors, warnings";
	}
	
	private String getUpdateColumnNames() {
		return "label=?, filePath=?, fileName=?, updated=?, imagetype=?, errors=?, warnings=?";
	}
	
	/**
	 * Convert ResultSet to a List of Disk.
	 * @param rs ResultSet
	 * @return List of Disk
	 * @throws SQLException
	 */
	private List<Disk> consumeRows(ResultSet rs) throws SQLException {
		List<Disk> list = new ArrayList<Disk>();
		while (rs.next()) {
			list.add(consumeRow(rs));
		}
		return list;
	}

	/**
	 * Convert one ResultSet to a Disk
	 * @param rs ResultSet
	 * @return Disk
	 * @throws SQLException
	 */
	private Disk consumeRow(ResultSet rs) throws SQLException {
		Disk vo = new Disk();
		vo.setDiskId(rs.getLong(1));
		vo.setLabel(rs.getString(2));
		vo.setFilePath(rs.getString(3));
		vo.setFileName(rs.getString(4));
		vo.setImageType(rs.getInt(5));
		vo.setErrors(getInteger(rs, 6));
		vo.setWarnings(getInteger(rs, 7));
		return vo;
	}
	
	/**
	 * Test if string is null or empty.
	 * @param str String
	 * @return true if trimmed String is empty.
	 */
	private boolean isStringNullOrEmpty(String str) {
		return str==null || str.trim().isEmpty();
	}
	
	private Integer getInteger(ResultSet rs, int col) throws SQLException {
        int value = rs.getInt(col);
        return rs.wasNull() ? null : value;
    }
	
	private void setInteger(PreparedStatement stmt, int col, Integer value) throws SQLException {
		if (value == null) {
			stmt.setNull(col, java.sql.Types.INTEGER); 
		} else {
			stmt.setInt(col,  value.intValue());
		}
	}
	
}
