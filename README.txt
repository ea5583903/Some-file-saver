DATABASE & FILE MANAGER APPLICATION
=====================================

A GUI application for managing SQLite databases, tables, and various file formats.

FEATURES:
---------
- SQLite database creation and management
- Table viewer and editor
- Support for .odb (OpenDocument Database) files
- SQL file import/export (.sql)
- PDF document management
- Text document handling
- File browser with finder
- Real-time file monitoring

HOW TO RUN:
-----------
Option 1: Using IntelliJ IDEA
  - Open the project in IntelliJ IDEA
  - Right-click on DatabaseApp.java
  - Select "Run 'DatabaseApp.main()'"

Option 2: Using the run script
  - Open terminal in the project directory
  - Run: ./run.sh

Option 3: Manual compilation
  - javac -cp "lib/sqlite-jdbc-3.45.1.0.jar" -d out src/*.java
  - java -cp "out:lib/sqlite-jdbc-3.45.1.0.jar" DatabaseApp

MAIN FEATURES:
--------------
1. File Finder (Left Panel)
   - Click "Add Folder" to browse workspace folders
   - Double-click files to open them
   - Refresh to update the file tree

2. Monitor (Bottom Panel)
   - Shows all application events and file operations
   - Timestamps for all activities

3. Menu Options:
   File Menu:
   - New File: Create a new blank file
   - New Database: Create a new SQLite database
   - Open File: Open any supported file
   - Save: Save current file to its original location
   - Save As: Save current file with a new name/location
   - Exit: Close the application

   Database Menu:
   - Create Table: Add a new table to the database
   - Import SQL: Import SQL file into database
   - Export SQL: Export database to SQL file

   Documents Menu:
   - Save as PDF: Export current content to PDF
   - Save Document: Save current document

4. Table Operations:
   - View all tables in the selected database
   - Create new tables with custom columns
   - Add/delete rows
   - Run custom SQL queries
   - Delete tables

SUPPORTED FILE TYPES:
---------------------
The application can open, edit, and save ANY file type EXCEPT images.

EXCLUDED (cannot save):
- .png, .jpg, .jpeg, .gif, .bmp, .ico, .svg, .webp, .tiff, .tif

SUPPORTED (can open and save):
- .db, .sqlite (SQLite databases)
- .odb (OpenDocument Database)
- .sql (SQL scripts)
- .pdf (PDF documents)
- .txt, .doc, .docx (Text documents)
- .java, .py, .js, .html, .css, .xml (Code files)
- .json, .yaml, .yml, .toml (Configuration files)
- .md, .rst (Markdown/documentation)
- .csv, .tsv (Data files)
- .log (Log files)
- ANY other text-based or binary file format (except images)

DEPENDENCIES:
-------------
- Java 25 (or compatible version)
- SQLite JDBC Driver (included in lib folder)

NOTES:
------
- All changes to database tables should be saved using the "Save Changes" button
- The file monitor tracks file modifications every 5 seconds
- SQL queries can be executed through the "Run Query" button in table viewer
